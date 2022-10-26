package fr.frinn.custommachinery.common.crafting.craft;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.crafting.ComponentNotFoundException;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IProcessor;
import fr.frinn.custommachinery.api.crafting.IProcessorTemplate;
import fr.frinn.custommachinery.api.crafting.ProcessorType;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.common.component.variant.item.ResultItemComponentVariant;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class CraftProcessor implements IProcessor {

    private final MachineTile tile;
    private boolean shouldCheck = true;
    @Nullable
    private CraftingContext currentContext;
    @Nullable
    private CustomCraftRecipe currentRecipe;

    public CraftProcessor(MachineTile tile) {
        this.tile = tile;
    }

    @Nullable
    @Override
    public ICraftingContext getCurrentContext() {
        return this.currentContext;
    }

    @Override
    public ProcessorType<CraftProcessor> getType() {
        return Registration.CRAFT_PROCESSOR.get();
    }

    @Override
    public MachineTile getTile() {
        return this.tile;
    }

    @Override
    public double getRecipeProgressTime() {
        return 0;
    }

    @Override
    public void tick() {
        if(this.shouldCheck) {
            this.shouldCheck = false;

            if(this.currentRecipe != null && this.currentContext != null) {
                if(checkRecipe(this.currentRecipe, this.currentContext))
                    return;
                else {
                    this.reset();
                }
            }

            findRecipe().ifPresent(this::setCurrentRecipe);
        }
    }

    @Override
    public void setMachineInventoryChanged() {
        this.shouldCheck = true;
    }

    public void craft() {
        if(this.currentRecipe == null || this.currentContext == null)
            return;

        this.processRecipe(this.currentRecipe, this.currentContext);

        this.reset();
    }

    public boolean bulkCraft() {
        if(this.currentRecipe == null || this.currentContext == null)
            return false;

        this.processRecipe(this.currentRecipe, this.currentContext);

        if(checkRecipe(this.currentRecipe, this.currentContext)) {
            this.setCurrentRecipe(this.currentRecipe);
            return true;
        } else {
            this.reset();
            return false;
        }
    }

    private Optional<CustomCraftRecipe> findRecipe() {
        if(this.tile.getLevel() == null)
            return Optional.empty();

        List<CustomCraftRecipe> recipes = this.tile.getLevel().getRecipeManager().getAllRecipesFor(Registration.CUSTOM_CRAFT_RECIPE.get())
                .stream()
                .filter(recipe -> recipe.getMachineId().equals(this.tile.getMachine().getId()))
                .toList();

        CraftingContext.Mutable context = new CraftingContext.Mutable(this, this.tile.getUpgradeManager());
        for(CustomCraftRecipe recipe : recipes) {
            context.setRecipe(recipe);
            if(checkRecipe(recipe, context)) return Optional.of(recipe);
        }

        return Optional.empty();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean checkRecipe(CustomCraftRecipe recipe, CraftingContext context) {
        return recipe.getRequirements().stream().allMatch(requirement -> {
            IMachineComponent component = this.tile.getComponentManager().getComponent(requirement.getComponentType()).orElseThrow(() -> new ComponentNotFoundException(recipe, this.tile.getMachine(), requirement.getType()));
            return ((IRequirement)requirement).test(component, context);
        });
    }

    private void setCurrentRecipe(CustomCraftRecipe recipe) {
        this.currentRecipe = recipe;
        this.currentContext = new CraftingContext(this, this.tile.getUpgradeManager(), recipe);
        this.tile.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponents().stream().filter(component -> component.getVariant() == ResultItemComponentVariant.INSTANCE).findFirst())
                .ifPresent(component -> component.setItemStack(recipe.getOutput().copy()));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void processRecipe(CustomCraftRecipe recipe, CraftingContext context) {
        recipe.getRequirements()
                .forEach(requirement -> {
                    IMachineComponent component = this.tile.getComponentManager().getComponent(requirement.getComponentType()).orElseThrow(() -> new ComponentNotFoundException(recipe, this.tile.getMachine(), requirement.getType()));
                    ((IRequirement)requirement).processStart(component, context);
                    ((IRequirement)requirement).processEnd(component, context);
                });
    }

    @Override
    public void reset() {
        this.currentRecipe = null;
        this.currentContext = null;
        this.tile.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponents().stream().filter(component -> component.getVariant() == ResultItemComponentVariant.INSTANCE).findFirst())
                .ifPresent(component -> component.setItemStack(ItemStack.EMPTY));
    }

    @Override
    public CompoundTag serialize() {
        return new CompoundTag();
    }

    @Override
    public void deserialize(CompoundTag nbt) {

    }

    public static class Template implements IProcessorTemplate<CraftProcessor> {

        public static final Codec<Template> CODEC = Codec.unit(Template::new);

        @Override
        public ProcessorType<CraftProcessor> getType() {
            return Registration.CRAFT_PROCESSOR.get();
        }

        @Override
        public CraftProcessor build(MachineTile tile) {
            return new CraftProcessor(tile);
        }
    }
}

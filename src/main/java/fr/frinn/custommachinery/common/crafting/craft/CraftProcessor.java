package fr.frinn.custommachinery.common.crafting.craft;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.crafting.IProcessor;
import fr.frinn.custommachinery.api.crafting.IProcessorTemplate;
import fr.frinn.custommachinery.api.crafting.ProcessorType;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.api.requirement.RecipeRequirement;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.crafting.RequirementList;
import fr.frinn.custommachinery.impl.crafting.RequirementList.RequirementWithFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class CraftProcessor implements IProcessor {

    private final MachineTile tile;
    private final Random rand = Utils.RAND;
    private final CraftingContext.Mutable mutableCraftingContext;
    private final CraftRecipeFinder recipeFinder;
    private boolean shouldCheck = true;
    private int recipeCheckCooldown = rand.nextInt(20);
    @Nullable
    private CraftingContext currentContext;
    @Nullable
    private RecipeHolder<CustomCraftRecipe> currentRecipe;
    private boolean initialized = false;

    public CraftProcessor(MachineTile tile) {
        this.tile = tile;
        this.mutableCraftingContext = new CraftingContext.Mutable(tile, tile.getUpgradeManager());
        this.recipeFinder = new CraftRecipeFinder(tile, 20);
    }

    @Override
    public ProcessorType<CraftProcessor> getType() {
        return Registration.CRAFT_PROCESSOR.get();
    }

    @Override
    public MachineTile tile() {
        return this.tile;
    }

    @Override
    public void tick() {
        if(!this.initialized) {
            this.recipeFinder.init();
            this.initialized = true;
        }

        if(currentRecipe == null)
            this.recipeFinder.findRecipe(this.mutableCraftingContext, this.shouldCheck).ifPresent(this::setCurrentRecipe);
        else if(this.mutableCraftingContext != null && (this.shouldCheck || this.recipeCheckCooldown-- == 0)) {
            this.recipeCheckCooldown = 20;
            if(!this.checkRecipe(this.currentRecipe, this.currentContext))
                this.reset();
        }

        this.shouldCheck = false;
    }

    @Override
    public void setMachineInventoryChanged() {
        this.shouldCheck = true;
        this.recipeFinder.setInventoryChanged(true);
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

    private boolean checkRecipe(RecipeHolder<CustomCraftRecipe> recipe, CraftingContext context) {
        return recipe.value().getRequirements().stream()
                .allMatch(requirement -> requirement.test(this.tile.getComponentManager(), context).isSuccess());
    }

    private void setCurrentRecipe(RecipeHolder<CustomCraftRecipe> recipe) {
        this.currentRecipe = recipe;
        this.currentContext = new CraftingContext(this.tile, this.tile.getUpgradeManager(), recipe, () -> 0.0);
        this.tile.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponents().stream().filter(component -> component.getType() == Registration.ITEM_RESULT_MACHINE_COMPONENT.get()).findFirst())
                .ifPresent(component -> component.setItemStack(recipe.value().getOutput().copy()));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void processRecipe(RecipeHolder<CustomCraftRecipe> recipe, CraftingContext context) {
        for(RecipeRequirement<?, ?> requirement : recipe.value().getRequirements()) {
            if (requirement.shouldSkip(this.tile.getComponentManager(), this.rand, context))
                continue;
            RequirementList list = new RequirementList();
            requirement.requirement().gatherRequirements(list);
            ((Map<Double, List< RequirementWithFunction>>)list.getProcessRequirements()).values()
                    .stream()
                    .flatMap(List::stream)
                    .forEach(r -> r.process(this.tile.getComponentManager(), context));
        }
    }

    @Override
    public void reset() {
        this.currentRecipe = null;
        this.currentContext = null;
        this.tile.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponents().stream().filter(component -> component.getType() == Registration.ITEM_RESULT_MACHINE_COMPONENT.get()).findFirst())
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

        public static final NamedCodec<Template> CODEC = NamedCodec.unit(Template::new, "Craft processor");
        public static final Template DEFAULT = new Template();

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

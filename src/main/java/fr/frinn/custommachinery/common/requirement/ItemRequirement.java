package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.client.integration.jei.wrapper.ItemIngredientWrapper;
import fr.frinn.custommachinery.common.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.requirement.AbstractChanceableRequirement;
import fr.frinn.custommachinery.impl.requirement.AbstractRequirement;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CraftingHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class ItemRequirement extends AbstractChanceableRequirement<ItemComponentHandler> implements IJEIIngredientRequirement<ItemStack> {

    public static final NamedCodec<ItemRequirement> CODEC = NamedCodec.record(itemRequirementInstance ->
            itemRequirementInstance.group(
                    RequirementIOMode.CODEC.fieldOf("mode").forGetter(AbstractRequirement::getMode),
                    NamedCodec.of(CraftingHelper.makeIngredientCodec(true)).fieldOf("item").forGetter(requirement -> requirement.ingredient),
                    NamedCodec.INT.fieldOf("amount").forGetter(requirement -> requirement.amount),
                    NamedCodec.doubleRange(0.0, 1.0).optionalFieldOf("chance", 1.0D).forGetter(AbstractChanceableRequirement::getChance),
                    NamedCodec.STRING.optionalFieldOf("slot", "").forGetter(requirement -> requirement.slot)
            ).apply(itemRequirementInstance, (mode, item, amount, chance, slot) -> {
                    ItemRequirement requirement = new ItemRequirement(mode, item, amount, slot);
                    requirement.setChance(chance);
                    return requirement;
            }), "Item requirement"
    );

    private final Ingredient ingredient;
    private final int amount;
    private final String slot;

    public ItemRequirement(RequirementIOMode mode, Ingredient ingredient, int amount, String slot) {
        super(mode);
        if(mode == RequirementIOMode.OUTPUT && ingredient.getItems().length > 1)
            throw new IllegalArgumentException("You can't use a Tag for an Output Item Requirement");
        this.ingredient = ingredient;
        this.amount = amount;
        this.slot = slot == null ? "" : slot;
    }

    @Override
    public RequirementType<ItemRequirement> getType() {
        return Registration.ITEM_REQUIREMENT.get();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public MachineComponentType getComponentType() {
        return Registration.ITEM_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean test(ItemComponentHandler component, ICraftingContext context) {
        int amount = (int)context.getIntegerModifiedValue(this.amount, this, null);
        if(getMode() == RequirementIOMode.INPUT) {
            return Arrays.stream(this.ingredient.getItems()).mapToInt(item -> component.getItemAmount(this.slot, item)).sum() >= amount;
        } else {
            if(this.ingredient.getItems().length > 0)
                return component.getSpaceForItem(this.slot, this.ingredient.getItems()[0]) >= amount;
            else throw new IllegalStateException("Can't use output empty item");
        }
    }

    @Override
    public CraftingResult processStart(ItemComponentHandler component, ICraftingContext context) {
        int amount = (int)context.getIntegerModifiedValue(this.amount, this, null);
        if(getMode() == RequirementIOMode.INPUT) {
            int maxExtract = Arrays.stream(this.ingredient.getItems()).mapToInt(item -> component.getItemAmount(this.slot, item)).sum();
            if(maxExtract >= amount) {
                int toExtract = amount;
                for (ItemStack item : this.ingredient.getItems()) {
                    int canExtract = component.getItemAmount(this.slot, item);
                    if(canExtract > 0) {
                        canExtract = Math.min(canExtract, toExtract);
                        component.removeFromInputs(this.slot, item, canExtract);
                        toExtract -= canExtract;
                        if(toExtract == 0)
                            return CraftingResult.success();
                    }
                }
            }
            return CraftingResult.error(Component.translatable("custommachinery.requirements.item.error.input", this.ingredient.toString(), amount, maxExtract));
        }
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(ItemComponentHandler component, ICraftingContext context) {
        int amount = (int)context.getIntegerModifiedValue(this.amount, this, null);
        if(getMode() == RequirementIOMode.OUTPUT) {
            if(this.ingredient.getItems().length > 0) {
                ItemStack item = this.ingredient.getItems()[0];
                int canInsert = component.getSpaceForItem(this.slot, item);
                if(canInsert >= amount) {
                    component.addToOutputs(this.slot, item.copy(), amount);
                    return CraftingResult.success();
                }
                return CraftingResult.error(Component.translatable("custommachinery.requirements.item.error.output", amount, Component.translatable(item.getDescriptionId())));
            } else throw new IllegalStateException("Can't use output item requirement with item tag");
        }
        return CraftingResult.pass();
    }

    @Override
    public List<IJEIIngredientWrapper<ItemStack>> getJEIIngredientWrappers(IMachineRecipe recipe) {
        return Collections.singletonList(new ItemIngredientWrapper(this.getMode(), this.ingredient, this.amount, getChance(), false, this.slot, true));
    }
}

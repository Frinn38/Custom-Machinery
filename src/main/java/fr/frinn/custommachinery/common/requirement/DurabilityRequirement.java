package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.crafting.IRequirementList;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RecipeRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.client.integration.jei.wrapper.ItemIngredientWrapper;
import fr.frinn.custommachinery.common.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.init.CMRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.Collections;
import java.util.List;

public class DurabilityRequirement implements IRequirement<ItemComponentHandler>, IJEIIngredientRequirement<ItemStack> {

    public static final NamedCodec<DurabilityRequirement> CODEC = NamedCodec.record(durabilityRequirementInstance ->
            durabilityRequirementInstance.group(
                    RequirementIOMode.CODEC.fieldOf("mode").forGetter(DurabilityRequirement::getMode),
                    NamedCodec.of(Ingredient.CODEC).fieldOf("ingredient").aliases("item").forGetter(requirement -> requirement.ingredient),
                    NamedCodec.intRange(1, Integer.MAX_VALUE).fieldOf("amount").forGetter(requirement -> requirement.amount),
                    NamedCodec.BOOL.optionalFieldOf("break", false).forGetter(requirement -> requirement.canBreak),
                    NamedCodec.STRING.optionalFieldOf("slot", "").forGetter(requirement -> requirement.slot)
            ).apply(durabilityRequirementInstance, DurabilityRequirement::new), "Durability requirement"
    );

    private final RequirementIOMode mode;
    private final Ingredient ingredient;
    private final int amount;
    private final String slot;
    private final boolean canBreak;

    public DurabilityRequirement(RequirementIOMode mode, Ingredient ingredient, int amount, boolean canBreak, String slot) {
        this.mode = mode;
        this.ingredient = ingredient;
        this.amount = amount;
        this.canBreak = canBreak;
        this.slot = slot;
    }

    @Override
    public RequirementType<DurabilityRequirement> getType() {
        return CMRegistration.DURABILITY_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType getComponentType() {
        return CMRegistration.ITEM_MACHINE_COMPONENT.get();
    }

    @Override
    public RequirementIOMode getMode() {
        return this.mode;
    }

    @Override
    public boolean test(ItemComponentHandler component, ICraftingContext context) {
        int amount = (int)context.getIntegerModifiedValue(this.amount, this, null);
        if(getMode() == RequirementIOMode.INPUT)
            return component.getDurabilityAmount(this.slot, this.ingredient) >= amount;
        else
            return component.getSpaceForDurability(this.slot, this.ingredient) >= amount;
    }

    @Override
    public void gatherRequirements(IRequirementList<ItemComponentHandler> list) {
        if(this.mode == RequirementIOMode.INPUT)
            list.processOnStart(this::processInputs);
        else
            list.processOnEnd(this::processOutputs);
    }

    public CraftingResult processInputs(ItemComponentHandler component, ICraftingContext context) {
        int amount = (int)context.getIntegerModifiedValue(this.amount, this, null);
        int maxRemove = component.getDurabilityAmount(this.slot, this.ingredient);
        if(maxRemove >= amount) {
            component.removeDurability(this.slot, this.ingredient, amount, this.canBreak);
            return CraftingResult.success();
        }
        return CraftingResult.error(Component.translatable("custommachinery.requirements.durability.error.input", amount, maxRemove));
    }

    public CraftingResult processOutputs(ItemComponentHandler component, ICraftingContext context) {
        int amount = (int)context.getIntegerModifiedValue(this.amount, this, null);
        int maxRepair = component.getSpaceForDurability(this.slot, this.ingredient);
        if(maxRepair >= amount) {
            component.repairItem(this.slot, this.ingredient, amount);
            return CraftingResult.success();
        }
        return CraftingResult.error(Component.translatable("custommachinery.requirements.durability.error.output", amount, maxRepair));
    }

    @Override
    public List<IJEIIngredientWrapper<ItemStack>> getJEIIngredientWrappers(IMachineRecipe recipe, RecipeRequirement<?, ?> requirement) {
        return Collections.singletonList(new ItemIngredientWrapper(this.getMode(), new SizedIngredient(this.ingredient, this.amount), requirement.chance(), true, this.slot, true));
    }
}

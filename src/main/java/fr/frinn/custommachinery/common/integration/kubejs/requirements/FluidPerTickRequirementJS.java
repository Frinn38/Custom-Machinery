package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.requirement.FluidPerTickRequirement;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

public interface FluidPerTickRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireFluidPerTick(FluidStack stack) {
        return this.requireFluidPerTick(stack, "");
    }

    default RecipeJSBuilder requireFluidPerTick(FluidStack stack, String tank) {
        return this.requireFluidIngredientPerTick(FluidIngredient.of(stack), stack.getAmount(), tank);
    }

    default RecipeJSBuilder requireFluidIngredientPerTick(FluidIngredient ingredient, int amount) {
        return this.requireFluidIngredientPerTick(ingredient, amount, "");
    }

    default RecipeJSBuilder requireFluidIngredientPerTick(FluidIngredient ingredient, int amount, String tank) {
        try {
            return this.addRequirement(new FluidPerTickRequirement(RequirementIOMode.INPUT, ingredient, amount, tank));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    default RecipeJSBuilder produceFluidPerTick(FluidStack stack) {
        return this.produceFluidPerTick(stack, "");
    }

    default RecipeJSBuilder produceFluidPerTick(FluidStack stack, String tank) {
        try {
            return this.addRequirement(new FluidPerTickRequirement(RequirementIOMode.OUTPUT, FluidIngredient.of(stack), stack.getAmount(), tank));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }
}

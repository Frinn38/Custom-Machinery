package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.requirement.FluidRequirement;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

public interface FluidRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireFluid(FluidStack stack) {
        return this.requireFluid(stack, "");
    }

    default RecipeJSBuilder requireFluid(FluidStack stack, String tank) {
        return this.requireFluidIngredient(FluidIngredient.of(stack), stack.getAmount(), tank);
    }

    default RecipeJSBuilder requireFluidIngredient(FluidIngredient ingredient, int amount) {
        return this.requireFluidIngredient(ingredient, amount, "");
    }

    default RecipeJSBuilder requireFluidIngredient(FluidIngredient ingredient, int amount, String tank) {
        try {
            return this.addRequirement(new FluidRequirement(RequirementIOMode.INPUT, ingredient, amount, tank));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    default RecipeJSBuilder produceFluid(FluidStack stack) {
        return this.produceFluid(stack, "");
    }

    default RecipeJSBuilder produceFluid(FluidStack stack, String tank) {
        try {
            return this.addRequirement(new FluidRequirement(RequirementIOMode.OUTPUT, FluidIngredient.of(stack), stack.getAmount(), tank));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }
}

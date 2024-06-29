package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.requirement.FluidRequirement;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public interface FluidRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireFluid(SizedFluidIngredient ingredient) {
        return this.requireFluid(ingredient, "");
    }

    default RecipeJSBuilder requireFluid(SizedFluidIngredient ingredient, String tank) {
        if(ingredient.ingredient().hasNoFluids())
            return this.error("Invalid empty fluid ingredient in fluid input requirement");
        try {
            return this.addRequirement(new FluidRequirement(RequirementIOMode.INPUT, ingredient, tank));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    default RecipeJSBuilder produceFluid(FluidStack stack) {
        return this.produceFluid(stack, "");
    }

    default RecipeJSBuilder produceFluid(FluidStack stack, String tank) {
        if(stack.isEmpty())
            return this.error("Invalid empty fluid in fluid output requirement");
        try {
            return this.addRequirement(new FluidRequirement(RequirementIOMode.OUTPUT, SizedFluidIngredient.of(stack), tank));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }
}

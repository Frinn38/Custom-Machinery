package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.requirement.FluidRequirement;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.Collections;
import java.util.List;

public interface FluidRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireFluid(SizedFluidIngredient ingredient) {
        return this.requireFluid(ingredient, Collections.emptyList());
    }

    default RecipeJSBuilder requireFluid(SizedFluidIngredient ingredient, List<String> tanks) {
        if(ingredient.ingredient().hasNoFluids())
            return this.error("Invalid empty fluid ingredient in fluid input requirement");
        try {
            return this.addRequirement(new FluidRequirement(RequirementIOMode.INPUT, ingredient, tanks));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    default RecipeJSBuilder produceFluid(FluidStack stack) {
        return this.produceFluid(stack, Collections.emptyList());
    }

    default RecipeJSBuilder produceFluid(FluidStack stack, List<String> tanks) {
        if(stack.isEmpty())
            return this.error("Invalid empty fluid in fluid output requirement");
        try {
            return this.addRequirement(new FluidRequirement(RequirementIOMode.OUTPUT, SizedFluidIngredient.of(stack), tanks));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }
}

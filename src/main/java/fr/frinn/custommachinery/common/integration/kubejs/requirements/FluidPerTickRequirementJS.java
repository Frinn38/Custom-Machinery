package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.requirement.FluidPerTickRequirement;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public interface FluidPerTickRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireFluidPerTick(SizedFluidIngredient ingredient) {
        return this.requireFluidPerTick(ingredient, "");
    }

    default RecipeJSBuilder requireFluidPerTick(SizedFluidIngredient ingredient, String tank) {
        if(ingredient.ingredient().hasNoFluids())
            return this.error("Invalid empty fluid ingredient in fluid input requirement");
        try {
            return this.addRequirement(new FluidPerTickRequirement(RequirementIOMode.INPUT, ingredient, tank));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    default RecipeJSBuilder produceFluidPerTick(FluidStack stack) {
        return this.produceFluidPerTick(stack, "");
    }

    default RecipeJSBuilder produceFluidPerTick(FluidStack stack, String tank) {
        if(stack.isEmpty())
            return this.error("Invalid empty fluid in fluid output requirement");
        try {
            return this.addRequirement(new FluidPerTickRequirement(RequirementIOMode.OUTPUT, SizedFluidIngredient.of(stack), tank));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }
}

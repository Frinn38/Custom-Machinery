package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import dev.latvian.mods.kubejs.util.MapJS;
import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.requirement.FluidRequirement;
import fr.frinn.custommachinery.common.util.ingredient.FluidIngredient;
import fr.frinn.custommachinery.common.util.ingredient.FluidTagIngredient;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Map;

public interface FluidRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireFluid(FluidStack stack) {
        return this.requireFluid(stack, "");
    }

    default RecipeJSBuilder requireFluid(FluidStack stack, String tank) {
        return this.addRequirement(new FluidRequirement(RequirementIOMode.INPUT, new FluidIngredient(stack.getFluid()), stack.getAmount(), null, tank));
    }

    default RecipeJSBuilder requireFluidTag(String tag, int amount) {
        return this.requireFluidTag(tag, amount, null, "");
    }

    default RecipeJSBuilder requireFluidTag(String tag, int amount, Object thing) {
        if(thing instanceof String)
            return this.requireFluidTag(tag, amount, null, (String)thing);
        else
            return this.requireFluidTag(tag, amount, MapJS.of(thing), "");
    }

    default RecipeJSBuilder requireFluidTag(String tag, int amount, Map<?,?> nbt, String tank) {
        try {
            return this.addRequirement(new FluidRequirement(RequirementIOMode.INPUT, FluidTagIngredient.create(tag), amount, null, tank));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    default RecipeJSBuilder produceFluid(FluidStack stack) {
        return this.produceFluid(stack, "");
    }

    default RecipeJSBuilder produceFluid(FluidStack stack, String tank) {
        return this.addRequirement(new FluidRequirement(RequirementIOMode.OUTPUT, new FluidIngredient(stack.getFluid()), stack.getAmount(), null, tank));
    }
}

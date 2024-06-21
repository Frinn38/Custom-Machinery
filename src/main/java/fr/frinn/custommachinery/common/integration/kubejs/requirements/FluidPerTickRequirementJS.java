package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import dev.latvian.mods.kubejs.util.MapJS;
import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.requirement.FluidPerTickRequirement;
import fr.frinn.custommachinery.common.util.ingredient.FluidIngredient;
import fr.frinn.custommachinery.common.util.ingredient.FluidTagIngredient;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Map;

public interface FluidPerTickRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireFluidPerTick(FluidStack stack) {
        return this.requireFluidPerTick(stack, "");
    }

    default RecipeJSBuilder requireFluidPerTick(FluidStack stack, String tank) {
        return this.addRequirement(new FluidPerTickRequirement(RequirementIOMode.INPUT, new FluidIngredient(stack.getFluid()), stack.getAmount(), null, tank));
    }

    default RecipeJSBuilder requireFluidTagPerTick(String tag, int amount) {
        return this.requireFluidTagPerTick(tag, amount, null, "");
    }

    default RecipeJSBuilder requireFluidTagPerTick(String tag, int amount, Object thing) {
        if(thing instanceof String)
            return this.requireFluidTagPerTick(tag, amount, null, (String)thing);
        else
            return this.requireFluidTagPerTick(tag, amount, MapJS.of(thing), "");
    }

    default RecipeJSBuilder requireFluidTagPerTick(String tag, int amount, Map<?,?> nbt, String tank) {
        try {
            return this.addRequirement(new FluidPerTickRequirement(RequirementIOMode.INPUT, FluidTagIngredient.create(tag), amount, null, tank));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    default RecipeJSBuilder produceFluidPerTick(FluidStack stack) {
        return this.produceFluidPerTick(stack, "");
    }

    default RecipeJSBuilder produceFluidPerTick(FluidStack stack, String tank) {
        return this.addRequirement(new FluidPerTickRequirement(RequirementIOMode.OUTPUT, new FluidIngredient(stack.getFluid()), stack.getAmount(), null, tank));
    }
}

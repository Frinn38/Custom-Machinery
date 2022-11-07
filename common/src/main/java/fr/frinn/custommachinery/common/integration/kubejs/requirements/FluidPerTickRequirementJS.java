package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import dev.latvian.mods.kubejs.fluid.FluidStackJS;
import dev.latvian.mods.kubejs.util.MapJS;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.FluidPerTickRequirement;
import fr.frinn.custommachinery.common.util.ingredient.FluidIngredient;
import fr.frinn.custommachinery.common.util.ingredient.FluidTagIngredient;

public interface FluidPerTickRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireFluidPerTick(FluidStackJS stack) {
        return this.requireFluidPerTick(stack, "");
    }

    default RecipeJSBuilder requireFluidPerTick(FluidStackJS stack, String tank) {
        return this.addRequirement(new FluidPerTickRequirement(RequirementIOMode.INPUT, new FluidIngredient(stack.getFluid()), (int)stack.getAmount(), stack.getFluidStack().getTag(), tank));
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

    default RecipeJSBuilder requireFluidTagPerTick(String tag, int amount, MapJS nbt, String tank) {
        try {
            return this.addRequirement(new FluidPerTickRequirement(RequirementIOMode.INPUT, FluidTagIngredient.create(tag), amount, nbt == null ? null : nbt.toNBT(), tank));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    default RecipeJSBuilder produceFluidPerTick(FluidStackJS stack) {
        return this.produceFluidPerTick(stack, "");
    }

    default RecipeJSBuilder produceFluidPerTick(FluidStackJS stack, String tank) {
        return this.addRequirement(new FluidPerTickRequirement(RequirementIOMode.OUTPUT, new FluidIngredient(stack.getFluid()), (int)stack.getAmount(), stack.getFluidStack().getTag(), tank));
    }
}

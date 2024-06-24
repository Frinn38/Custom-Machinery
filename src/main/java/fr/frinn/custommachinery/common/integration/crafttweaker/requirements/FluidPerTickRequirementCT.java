package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import fr.frinn.custommachinery.api.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.common.requirement.FluidPerTickRequirement;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.OptionalString;

@ZenRegister
@Name(CTConstants.REQUIREMENT_FLUID_PER_TICK)
public interface FluidPerTickRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T requireFluidPerTick(IFluidStack fluid, @OptionalString String tank) {
        return addRequirement(new FluidPerTickRequirement(RequirementIOMode.INPUT, net.neoforged.neoforge.fluids.crafting.FluidIngredient.of((FluidStack)fluid.getImmutableInternal()), (int)fluid.getAmount(), tank));
    }

    @Method
    default T requireFluidPerTick(CTFluidIngredient ingredient, int amount, @OptionalString String tank) {
        try {
            return addRequirement(new FluidPerTickRequirement(RequirementIOMode.INPUT, net.neoforged.neoforge.fluids.crafting.FluidIngredient.of(ingredient.getMatchingStacks().stream().map(fluid -> (FluidStack)fluid.getImmutableInternal()).toArray(FluidStack[]::new)), amount, tank));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    @Method
    default T produceFluidPerTick(IFluidStack fluid, @OptionalString String tank) {
        return addRequirement(new FluidPerTickRequirement(RequirementIOMode.OUTPUT, FluidIngredient.of((FluidStack)fluid.getImmutableInternal()), (int)fluid.getAmount(), tank));
    }
}

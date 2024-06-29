package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import fr.frinn.custommachinery.api.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.common.requirement.FluidRequirement;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.OptionalString;

@ZenRegister
@Name(CTConstants.REQUIREMENT_FLUID)
public interface FluidRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T requireFluid(IFluidStack fluid, @OptionalString String tank) {
        return addRequirement(new FluidRequirement(RequirementIOMode.INPUT, SizedFluidIngredient.of(fluid.getImmutableInternal()), tank));
    }

    @Method
    default T produceFluid(IFluidStack fluid, @OptionalString String tank) {
        return addRequirement(new FluidRequirement(RequirementIOMode.OUTPUT, SizedFluidIngredient.of(fluid.getImmutableInternal()), tank));
    }
}

package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.base.IData;
import com.blamejared.crafttweaker.api.tag.MCTag;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTUtils;
import fr.frinn.custommachinery.api.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.common.requirement.FluidRequirement;
import fr.frinn.custommachinery.common.util.ingredient.FluidIngredient;
import fr.frinn.custommachinery.common.util.ingredient.FluidTagIngredient;
import net.minecraft.world.level.material.Fluid;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.Optional;
import org.openzen.zencode.java.ZenCodeType.OptionalString;

@ZenRegister
@Name(CTConstants.REQUIREMENT_FLUID)
public interface FluidRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T requireFluid(Fluid fluid, long amount, @Optional IData data, @OptionalString String tank) {
        return addRequirement(new FluidRequirement(RequirementIOMode.INPUT, new FluidIngredient(fluid), amount, CTUtils.getNBT(data), tank));
    }

    @Method
    default T requireFluidTag(MCTag tag, long amount, @Optional IData data, @OptionalString String tank) {
        try {
            return addRequirement(new FluidRequirement(RequirementIOMode.INPUT, FluidTagIngredient.create(tag.getTagKey()), amount, CTUtils.getNBT(data), tank));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    @Method
    default T produceFluid(Fluid fluid, long amount, @Optional IData data, @OptionalString String tank) {
        return addRequirement(new FluidRequirement(RequirementIOMode.OUTPUT, new FluidIngredient(fluid), amount, CTUtils.getNBT(data), tank));
    }
}

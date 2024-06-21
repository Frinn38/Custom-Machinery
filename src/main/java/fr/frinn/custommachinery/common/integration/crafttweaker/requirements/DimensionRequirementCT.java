package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import fr.frinn.custommachinery.api.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.common.requirement.DimensionRequirement;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ZenRegister
@Name(CTConstants.REQUIREMENT_DIMENSION)
public interface DimensionRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T dimensionWhitelist(String[] dimensions) {
        try {
            List<ResourceLocation> dimensionsID = Arrays.stream(dimensions).map(ResourceLocation::parse).toList();
            return addRequirement(new DimensionRequirement(dimensionsID, false));
        } catch (ResourceLocationException e) {
            return error("Invalid dimension ID: {}", e.getMessage());
        }
    }

    @Method
    default T dimensionWhitelist(String dimension) {
        try {
            return addRequirement(new DimensionRequirement(Collections.singletonList(ResourceLocation.parse(dimension)), false));
        } catch (ResourceLocationException e) {
            return error("Invalid dimension ID: {}", e.getMessage());
        }
    }

    @Method
    default T dimensionBlacklist(String[] dimensions) {
        try {
            List<ResourceLocation> dimensionsID = Arrays.stream(dimensions).map(ResourceLocation::parse).toList();
            return addRequirement(new DimensionRequirement(dimensionsID, false));
        } catch (ResourceLocationException e) {
            return error("Invalid dimension ID: {}", e.getMessage());
        }
    }

    @Method
    default T dimensionBlacklist(String dimension) {
        try {
            return addRequirement(new DimensionRequirement(Collections.singletonList(ResourceLocation.parse(dimension)), false));
        } catch (ResourceLocationException e) {
            return error("Invalid dimension ID: {}", e.getMessage());
        }
    }
}

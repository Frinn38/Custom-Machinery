package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.common.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.DimensionRequirement;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public interface DimensionRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder dimensionWhitelist(String[] dimensions) {
        List<ResourceLocation> dimensionsID = new ArrayList<>();
        for(String dimension : dimensions) {
            if(ResourceLocation.isValidResourceLocation(dimension))
                dimensionsID.add(new ResourceLocation(dimension));
            else
                return error("Invalid dimension ID: {}", dimension);
        }
        return this.addRequirement(new DimensionRequirement(dimensionsID, false));
    }

    default RecipeJSBuilder dimensionBlacklist(String[] dimensions) {
        List<ResourceLocation> dimensionsID = new ArrayList<>();
        for(String dimension : dimensions) {
            if(ResourceLocation.isValidResourceLocation(dimension))
                dimensionsID.add(new ResourceLocation(dimension));
            else
                return error("Invalid dimension ID: {}", dimension);
        }
        return this.addRequirement(new DimensionRequirement(dimensionsID, true));
    }
}

package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.DimensionRequirement;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public interface DimensionRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder dimensionWhitelist(String[] dimensions) {
        List<ResourceLocation> dimensionsID = new ArrayList<>();
        for(String dimension : dimensions) {
            try {
                dimensionsID.add(ResourceLocation.parse(dimension));
            } catch (ResourceLocationException e) {
                return error("Invalid dimension ID: {}", dimension);
            }
        }
        return this.addRequirement(new DimensionRequirement(dimensionsID, false));
    }

    default RecipeJSBuilder dimensionBlacklist(String[] dimensions) {
        List<ResourceLocation> dimensionsID = new ArrayList<>();
        for(String dimension : dimensions) {
            try {
                dimensionsID.add(ResourceLocation.parse(dimension));
            } catch (ResourceLocationException e) {
                return error("Invalid dimension ID: {}", dimension);
            }
        }
        return this.addRequirement(new DimensionRequirement(dimensionsID, true));
    }
}

package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import dev.latvian.mods.kubejs.script.ScriptType;
import fr.frinn.custommachinery.common.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.DimensionRequirement;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.List;

public interface DimensionRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder dimensionWhitelist(String[] dimensions) {
        List<ResourceLocation> dimensionsID = Arrays.stream(dimensions).filter(dimension -> {
            if(Utils.isResourceNameValid(dimension))
                return true;
            ScriptType.SERVER.console.warn("Invalid dimension ID: " + dimension);
            return false;
        }).map(ResourceLocation::new).toList();
        return this.addRequirement(new DimensionRequirement(dimensionsID, false));
    }

    default RecipeJSBuilder dimensionBlacklist(String[] dimensions) {
        List<ResourceLocation> dimensionsID = Arrays.stream(dimensions).filter(dimension -> {
            if(Utils.isResourceNameValid(dimension))
                return true;
            ScriptType.SERVER.console.warn("Invalid dimension ID: " + dimension);
            return false;
        }).map(ResourceLocation::new).toList();
        return this.addRequirement(new DimensionRequirement(dimensionsID, true));
    }
}

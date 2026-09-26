package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.DimensionRequirement;
import net.minecraft.resources.Identifier;

import java.util.List;

public interface DimensionRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder dimensionWhitelist(List<Identifier> dimensions) {
        return this.addRequirement(new DimensionRequirement(dimensions, false));
    }

    default RecipeJSBuilder dimensionBlacklist(List<Identifier> dimensions) {
        return this.addRequirement(new DimensionRequirement(dimensions, true));
    }
}

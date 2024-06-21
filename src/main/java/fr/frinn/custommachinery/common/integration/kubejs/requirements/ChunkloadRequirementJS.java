package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.ChunkloadRequirement;

public interface ChunkloadRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder chunkload() {
        return chunkload(1);
    }

    default RecipeJSBuilder chunkload(int radius) {
        if(radius < 1 || radius > 32)
            return error("Invalid radius for chunkload requirement: {}.\nMust be between 1 and 32", radius);
        return addRequirement(new ChunkloadRequirement(radius));
    }
}

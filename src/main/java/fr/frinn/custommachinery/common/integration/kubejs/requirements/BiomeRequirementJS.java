package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.BiomeRequirement;
import net.minecraft.resources.Identifier;

import java.util.List;

public interface BiomeRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder biomeWhitelist(List<Identifier> biomes) {
        return this.addRequirement(new BiomeRequirement(biomes, false));
    }

    default RecipeJSBuilder biomeBlacklist(List<Identifier> biomes) {
        return this.addRequirement(new BiomeRequirement(biomes, true));
    }
}

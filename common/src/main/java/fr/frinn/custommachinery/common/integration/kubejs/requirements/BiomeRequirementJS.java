package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import dev.latvian.mods.kubejs.script.ScriptType;
import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.BiomeRequirement;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.List;

public interface BiomeRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder biomeWhitelist(String[] biomes) {
        List<ResourceLocation> biomesID = Arrays.stream(biomes).filter(biome -> {
            if(Utils.isResourceNameValid(biome))
                return true;
            ScriptType.SERVER.console.warn("Invalid biome ID: " + biome);
            return false;
        }).map(ResourceLocation::new).toList();
        return this.addRequirement(new BiomeRequirement(biomesID, false));
    }

    default RecipeJSBuilder biomeBlacklist(String[] biomes) {
        List<ResourceLocation> biomesID = Arrays.stream(biomes).filter(biome -> {
            if(Utils.isResourceNameValid(biome))
                return true;
            ScriptType.SERVER.console.warn("Invalid biome ID: " + biome);
            return false;
        }).map(ResourceLocation::new).toList();
        return this.addRequirement(new BiomeRequirement(biomesID, true));
    }
}

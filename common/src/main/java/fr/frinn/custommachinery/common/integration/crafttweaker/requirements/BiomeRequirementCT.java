package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTUtils;
import fr.frinn.custommachinery.api.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.common.requirement.BiomeRequirement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ZenRegister
@Name(CTConstants.REQUIREMENT_BIOME)
public interface BiomeRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T biomeWhitelist(Biome[] biomes) {
        List<ResourceLocation> biomesID = Arrays.stream(biomes).map(CTUtils::biomeID).toList();
        return addRequirement(new BiomeRequirement(biomesID, false));
    }

    @Method
    default T biomeWhitelist(Biome biome) {
        return addRequirement(new BiomeRequirement(Collections.singletonList(CTUtils.biomeID(biome)), false));
    }

    @Method
    default T biomeBlacklist(Biome[] biomes) {
        List<ResourceLocation> biomesID = Arrays.stream(biomes).map(CTUtils::biomeID).toList();
        return addRequirement(new BiomeRequirement(biomesID, true));
    }

    @Method
    default T biomeBlacklist(Biome biome) {
        return addRequirement(new BiomeRequirement(Collections.singletonList(CTUtils.biomeID(biome)), true));
    }
}

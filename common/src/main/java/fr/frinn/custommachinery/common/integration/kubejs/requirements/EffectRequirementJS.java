package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import dev.latvian.mods.kubejs.script.ScriptType;
import fr.frinn.custommachinery.common.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.EffectRequirement;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface EffectRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder giveEffectOnEnd(String effect, int time, int radius) {
        return this.giveEffectOnEnd(effect, time, radius, 1, new String[]{});
    }

    default RecipeJSBuilder giveEffectOnEnd(String effect, int time, int radius, int level) {
        return this.giveEffectOnEnd(effect, time, radius, level, new String[]{});
    }

    default RecipeJSBuilder giveEffectOnEnd(String effect, int time, int radius, String[] filter) {
        return this.giveEffectOnEnd(effect, time, radius, 1, filter);
    }

    default RecipeJSBuilder giveEffectOnEnd(String effect, int time, int radius, int level, String[] filter) {
        List<EntityType<?>> entityFilter = Arrays.stream(filter).filter(type -> {
            if(Utils.isResourceNameValid(type) && Registry.ENTITY_TYPE.containsKey(new ResourceLocation(type)))
                return true;
            ScriptType.SERVER.console.warn("Invalid entity ID: " + type);
            return false;
        }).map(ResourceLocation::new).map(Registry.ENTITY_TYPE::get).collect(Collectors.toList());
        if(Utils.isResourceNameValid(effect) && Registry.MOB_EFFECT.containsKey(new ResourceLocation(effect)))
            return this.addRequirement(new EffectRequirement(Registry.MOB_EFFECT.get(new ResourceLocation(effect)), time, level, radius, entityFilter, true));
        else
            ScriptType.SERVER.console.warn("Invalid effect ID: " + effect);
        return this;
    }

    default RecipeJSBuilder giveEffectEachTick(String effect, int time, int radius) {
        return this.giveEffectEachTick(effect, time, radius, 1, new String[]{});
    }

    default RecipeJSBuilder giveEffectEachTick(String effect, int time, int radius, Object levelOrFilter) {
        if(levelOrFilter instanceof Number)
            return this.giveEffectEachTick(effect, time, radius, ((Number)levelOrFilter).intValue(), new String[]{});
        else if(levelOrFilter instanceof String)
            return this.giveEffectEachTick(effect, time, radius, 1, new String[]{(String)levelOrFilter});
        else if(levelOrFilter instanceof String[])
            return this.giveEffectEachTick(effect, time, radius, 1, (String[])levelOrFilter);
        ScriptType.SERVER.console.error("Invalid 4th param given to 'giveEffectEachTick' : " + levelOrFilter.toString());
        return this;
    }

    default RecipeJSBuilder giveEffectEachTick(String effect, int time, int radius, int level, String[] filter) {
        List<EntityType<?>> entityFilter = Arrays.stream(filter).filter(type -> {
            if(Utils.isResourceNameValid(type) && Registry.ENTITY_TYPE.containsKey(new ResourceLocation(type)))
                return true;
            ScriptType.SERVER.console.warn("Invalid entity ID: " + type);
            return false;
        }).map(ResourceLocation::new).map(Registry.ENTITY_TYPE::get).collect(Collectors.toList());
        if(Utils.isResourceNameValid(effect) && Registry.MOB_EFFECT.containsKey(new ResourceLocation(effect)))
            return this.addRequirement(new EffectRequirement(Registry.MOB_EFFECT.get(new ResourceLocation(effect)), time, level, radius, entityFilter, false));
        else
            ScriptType.SERVER.console.warn("Invalid effect ID: " + effect);
        return this;
    }
}

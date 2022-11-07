package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.common.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.EffectRequirement;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

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
        List<EntityType<?>> entityFilter = new ArrayList<>();
        for(String type : filter) {
            if(Utils.isResourceNameValid(type) && Registry.ENTITY_TYPE.containsKey(new ResourceLocation(type)))
                entityFilter.add(Registry.ENTITY_TYPE.get(new ResourceLocation(type)));
            else
                return error("Invalid entity ID: {}", type);
        }
        if(ResourceLocation.isValidResourceLocation(effect) && Registry.MOB_EFFECT.containsKey(new ResourceLocation(effect)))
            return this.addRequirement(new EffectRequirement(Registry.MOB_EFFECT.get(new ResourceLocation(effect)), time, level, radius, entityFilter, true));
        else
            return error("Invalid effect ID: {}", effect);
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
        return error("Invalid 4th param given to 'giveEffectEachTick' : {}", levelOrFilter.toString());
    }

    default RecipeJSBuilder giveEffectEachTick(String effect, int time, int radius, int level, String[] filter) {
        List<EntityType<?>> entityFilter = new ArrayList<>();
        for(String type : filter) {
            if(Utils.isResourceNameValid(type) && Registry.ENTITY_TYPE.containsKey(new ResourceLocation(type)))
                entityFilter.add(Registry.ENTITY_TYPE.get(new ResourceLocation(type)));
            else
                return error("Invalid entity ID: {}", type);
        }
        if(ResourceLocation.isValidResourceLocation(effect) && Registry.MOB_EFFECT.containsKey(new ResourceLocation(effect)))
            return this.addRequirement(new EffectRequirement(Registry.MOB_EFFECT.get(new ResourceLocation(effect)), time, level, radius, entityFilter, false));
        else
            return error("Invalid effect ID: {}", effect);
    }
}

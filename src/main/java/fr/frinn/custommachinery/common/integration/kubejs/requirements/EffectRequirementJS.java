package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.EffectRequirement;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;

import java.util.Collections;
import java.util.List;

public interface EffectRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder giveEffectOnEnd(MobEffect effect, int time, int radius) {
        return this.giveEffectOnEnd(effect, time, radius, 1);
    }

    default RecipeJSBuilder giveEffectOnEnd(MobEffect effect, int time, int radius, int level) {
        return this.giveEffectOnEnd(effect, time, radius, level, Collections.emptyList());
    }

    default RecipeJSBuilder giveEffectOnEnd(MobEffect effect, int time, int radius, int level, List<EntityType<?>> filter) {
        return this.addRequirement(new EffectRequirement(Holder.direct(effect), time, level, radius, filter, true));
    }

    default RecipeJSBuilder giveEffectEachTick(MobEffect effect, int time, int radius) {
        return this.giveEffectEachTick(effect, time, radius, 1);
    }

    default RecipeJSBuilder giveEffectEachTick(MobEffect effect, int time, int radius, int level) {
        return this.giveEffectEachTick(effect, time, radius, level, Collections.emptyList());
    }

    default RecipeJSBuilder giveEffectEachTick(MobEffect effect, int time, int radius, int level, List<EntityType<?>> filter) {
        return this.addRequirement(new EffectRequirement(Holder.direct(effect), time, level, radius, filter, false));
    }
}

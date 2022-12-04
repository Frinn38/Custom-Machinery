package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.api.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.common.requirement.EffectRequirement;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.Optional;
import org.openzen.zencode.java.ZenCodeType.OptionalInt;

import java.util.Arrays;

@ZenRegister
@Name(CTConstants.REQUIREMENT_EFFECT)
public interface EffectRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T giveEffectOnEnd(MobEffect effect, int time, int radius, @OptionalInt(1) int level, @Optional EntityType[] filter) {
        return addRequirement(new EffectRequirement(effect, time, level, radius, Arrays.asList(filter), true));
    }

    @Method
    default T giveEffectEachTick(MobEffect effect, int time, int radius, @OptionalInt(1) int level, @Optional EntityType[] filter) {
        return addRequirement(new EffectRequirement(effect, time, level, radius, Arrays.asList(filter), false));
    }
}

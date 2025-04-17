package fr.frinn.custommachinery.common.util.sound;

import com.mojang.datafixers.util.Either;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.function.Function;

public record AmbientSound(SoundEvent sound, float volume, float pitch, SoundSource source, boolean loop, boolean attenuation, int delay, boolean relative) {

    public static final AmbientSound DEFAULT = makeDefault(SoundEvent.createVariableRangeEvent(ResourceLocation.parse("")));

    public static final NamedCodec<AmbientSound> FULL_CODEC = NamedCodec.record(ambientSoundInstance ->
            ambientSoundInstance.group(
                    DefaultCodecs.SOUND_EVENT.fieldOf("sound").forGetter(AmbientSound::sound),
                    NamedCodec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("volume", 1F).forGetter(AmbientSound::volume),
                    NamedCodec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("pitch", 1F).forGetter(AmbientSound::pitch),
                    NamedCodec.enumCodec(SoundSource.class).optionalFieldOf("source", SoundSource.BLOCKS).forGetter(AmbientSound::source),
                    NamedCodec.BOOL.optionalFieldOf("loop", true).forGetter(AmbientSound::loop),
                    NamedCodec.BOOL.optionalFieldOf("attenuation", true).forGetter(AmbientSound::attenuation),
                    NamedCodec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("delay", 0).forGetter(AmbientSound::delay),
                    NamedCodec.BOOL.optionalFieldOf("relative", false).forGetter(AmbientSound::relative)
            ).apply(ambientSoundInstance, AmbientSound::new), "Ambient sound"
    );

    public static final NamedCodec<AmbientSound> CODEC = NamedCodec.either(FULL_CODEC, DefaultCodecs.SOUND_EVENT)
            .xmap(either -> either.map(Function.identity(), AmbientSound::makeDefault), Either::left, "Ambient sound");

    public static AmbientSound makeDefault(SoundEvent sound) {
        return new AmbientSound(sound, 1, 1, SoundSource.BLOCKS, true, true, 0, false);
    }
}

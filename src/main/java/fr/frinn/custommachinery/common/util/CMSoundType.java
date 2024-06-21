package fr.frinn.custommachinery.common.util;

import com.mojang.datafixers.util.Either;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.codec.NamedMapCodec;
import fr.frinn.custommachinery.impl.codec.NamedRecordCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Function;

public class CMSoundType extends SoundType {

    public static final CMSoundType DEFAULT = new CMSoundType(new PartialBlockState(Blocks.IRON_BLOCK));

    public static final NamedCodec<CMSoundType> FROM_STATE = PartialBlockState.CODEC.xmap(CMSoundType::new, type -> type.defaultBlock, "Sound type");

    public static final NamedMapCodec<CMSoundType> FROM_PARTS = NamedCodec.record(cmSoundTypeInstance ->
            cmSoundTypeInstance.group(
                    NamedCodec.FLOAT.optionalFieldOf("volume", 1.0F).forGetter(SoundType::getVolume),
                    NamedCodec.FLOAT.optionalFieldOf("pitch", 1.0F).forGetter(SoundType::getPitch),
                    partCodec("break", SoundType::getBreakSound),
                    partCodec("step", SoundType::getStepSound),
                    partCodec("place", SoundType::getPlaceSound),
                    partCodec("hit", SoundType::getHitSound),
                    partCodec("fall", SoundType::getFallSound)
            ).apply(cmSoundTypeInstance, CMSoundType::new), "Sound type"
    );

    public static final NamedCodec<CMSoundType> CODEC = NamedCodec.either(FROM_STATE, FROM_PARTS, "Interaction sounds").xmap(
            either -> either.map(Function.identity(), Function.identity()),
            Either::right,
            "Interaction sounds"
    );

    private final PartialBlockState defaultBlock;

    public CMSoundType(float volume, float pitch, SoundEvent breakSound, SoundEvent stepSound, SoundEvent placeSound, SoundEvent hitSound, SoundEvent fallSound) {
        super(volume, pitch, breakSound, stepSound, placeSound, hitSound, fallSound);
        this.defaultBlock = PartialBlockState.AIR;
    }

    public CMSoundType(PartialBlockState state) {
        super(1.0F, 1.0F, state.getBlockState().getSoundType().getBreakSound(), state.getBlockState().getSoundType().getStepSound(), state.getBlockState().getSoundType().getPlaceSound(), state.getBlockState().getSoundType().getHitSound(), state.getBlockState().getSoundType().getFallSound());
        this.defaultBlock = state;
    }

    private static NamedRecordCodec<CMSoundType, SoundEvent> partCodec(String field, Function<SoundType, SoundEvent> typeToSound) {
        return NamedCodec.either(PartialBlockState.CODEC, DefaultCodecs.SOUND_EVENT, StringUtils.capitalize(field) + " Sound").xmap(
                either -> either.map(state -> typeToSound.apply(state.getBlockState().getSoundType()), Function.identity()),
                Either::right,
                StringUtils.capitalize(field) + " Sound"
        ).optionalFieldOf(field, typeToSound.apply(DEFAULT)).forGetter(typeToSound::apply);
    }
}

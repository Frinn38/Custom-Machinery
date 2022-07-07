package fr.frinn.custommachinery.common.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.apiimpl.codec.CodecLogger;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Function;

public class CMSoundType extends SoundType {

    public static final CMSoundType DEFAULT = new CMSoundType(new PartialBlockState(Blocks.IRON_BLOCK));

    public static final Codec<CMSoundType> FROM_STATE = Codecs.PARTIAL_BLOCK_STATE_CODEC.xmap(CMSoundType::new, type -> type.defaultBlock);

    public static final Codec<CMSoundType> FROM_PARTS = RecordCodecBuilder.create(cmSoundTypeInstance ->
            cmSoundTypeInstance.group(
                    CodecLogger.loggedOptional(Codec.FLOAT, "volume", 1.0F).forGetter(SoundType::getVolume),
                    CodecLogger.loggedOptional(Codec.FLOAT, "pitch", 1.0F).forGetter(SoundType::getPitch),
                    partCodec("break", SoundType::getBreakSound),
                    partCodec("step", SoundType::getStepSound),
                    partCodec("place", SoundType::getPlaceSound),
                    partCodec("hit", SoundType::getHitSound),
                    partCodec("fall", SoundType::getFallSound)
            ).apply(cmSoundTypeInstance, CMSoundType::new)
    );

    public static final Codec<CMSoundType> CODEC = Codecs.either(FROM_STATE, FROM_PARTS, "Interaction Sounds").xmap(
            either -> either.map(Function.identity(), Function.identity()),
            Either::right
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

    private static RecordCodecBuilder<CMSoundType, SoundEvent> partCodec(String field, Function<SoundType, SoundEvent> typeToSound) {
        return CodecLogger.loggedOptional(Codecs.either(Codecs.PARTIAL_BLOCK_STATE_CODEC, SoundEvent.CODEC, StringUtils.capitalize(field) + " Sound").xmap(
                either -> either.map(state -> typeToSound.apply(state.getBlockState().getSoundType()), Function.identity()),
                Either::right
        ), field, typeToSound.apply(DEFAULT)).forGetter(typeToSound::apply);
    }
}

package fr.frinn.custommachinery.common.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Optional;

public class SoundManager {

    private BlockPos pos;
    private ISound sound;

    public SoundManager(BlockPos pos) {
        this.pos = pos;
    }

    @Nullable
    public ResourceLocation getSoundID() {
        return this.getSound().map(ISound::getSoundLocation).orElse(null);
    }

    public Optional<ISound> getSound() {
        return Optional.ofNullable(this.sound);
    }

    public void setSound(SoundEvent sound) {
        this.sound = new SimpleSound(sound, SoundCategory.BLOCKS, 1.0F, 1.0F, this.pos);
    }

    public boolean isPlaying() {
        return getSound().map(sound -> Minecraft.getInstance().getSoundHandler().isPlaying(sound)).orElse(false);
    }

    public void play() {
        getSound().ifPresent(sound -> Minecraft.getInstance().getSoundHandler().play(sound));
    }

    public void stop() {
        getSound().ifPresent(sound -> Minecraft.getInstance().getSoundHandler().stop(sound));
    }
}

package fr.frinn.custommachinery.common.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import javax.annotation.Nullable;
import java.util.Optional;

public class SoundManager {

    private BlockPos pos;
    private SoundInstance sound;

    public SoundManager(BlockPos pos) {
        this.pos = pos;
    }

    @Nullable
    public ResourceLocation getSoundID() {
        return this.getSound().map(SoundInstance::getLocation).orElse(null);
    }

    public Optional<SoundInstance> getSound() {
        return Optional.ofNullable(this.sound);
    }

    public void setSound(SoundEvent sound) {
        this.sound = new SimpleSoundInstance(sound, SoundSource.BLOCKS, 1.0F, 1.0F, this.pos);
    }

    public boolean isPlaying() {
        return getSound().map(sound -> Minecraft.getInstance().getSoundManager().isActive(sound)).orElse(false);
    }

    public void play() {
        getSound().ifPresent(sound -> Minecraft.getInstance().getSoundManager().play(sound));
    }

    public void stop() {
        getSound().ifPresent(sound -> Minecraft.getInstance().getSoundManager().stop(sound));
    }
}

package fr.frinn.custommachinery.common.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SoundManager {

    private final BlockPos pos;
    @Nullable
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

    public void setSound(@Nullable SoundEvent sound) {
        stop();

        if(sound == null) {
            this.sound = null;
            return;
        }

        this.sound = new SimpleSoundInstance(sound.getLocation(), SoundSource.BLOCKS, 1.0F, 1.0F, RandomSource.create(), true, 0, Attenuation.LINEAR, this.pos.getX(), this.pos.getY(), this.pos.getZ(), false);
        play();
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

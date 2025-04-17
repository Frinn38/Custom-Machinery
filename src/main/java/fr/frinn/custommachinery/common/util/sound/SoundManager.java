package fr.frinn.custommachinery.common.util.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.core.BlockPos;
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

    public boolean isCurrentlyPlaying(AmbientSound sound) {
        return this.sound != null
                && this.sound.getLocation().equals(sound.sound().getLocation())
                && this.sound.getVolume() == sound.volume()
                && this.sound.getPitch() == sound.pitch()
                && this.sound.getSource() == sound.source()
                && this.sound.isLooping() == sound.loop()
                && this.sound.getAttenuation() == (sound.attenuation() ? Attenuation.LINEAR : Attenuation.NONE)
                && this.sound.getDelay() == sound.delay()
                && this.sound.isRelative() == sound.relative();
    }

    public Optional<SoundInstance> getSound() {
        return Optional.ofNullable(this.sound);
    }

    public void setSound(@Nullable AmbientSound sound) {
        stop();

        if(sound == null) {
            this.sound = null;
            return;
        }

        this.sound = new SimpleSoundInstance(sound.sound().getLocation(), sound.source(), sound.volume(), sound.pitch(), RandomSource.create(), sound.loop(), sound.delay(), sound.attenuation() ? Attenuation.LINEAR : Attenuation.NONE, this.pos.getX(), this.pos.getY(), this.pos.getZ(), sound.relative());
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

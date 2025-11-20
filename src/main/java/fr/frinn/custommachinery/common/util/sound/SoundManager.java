package fr.frinn.custommachinery.common.util.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

public class SoundManager {

    private static final SoundInstance DEFAULT = SimpleSoundInstance.forMusic(AmbientSound.DEFAULT.sound());

    private final BlockPos pos;
    private SoundInstance sound = DEFAULT;

    public SoundManager(BlockPos pos) {
        this.pos = pos;
    }

    public boolean isCurrentlyPlaying(AmbientSound sound) {
        return this.sound != DEFAULT
                && this.sound.getSound() != null //Needed as that can be null in some weird cases
                && this.sound.getLocation().equals(sound.sound().getLocation())
                && this.sound.getVolume() == sound.volume()
                && this.sound.getPitch() == sound.pitch()
                && this.sound.getSource() == sound.source()
                && this.sound.isLooping() == sound.loop()
                && this.sound.getAttenuation() == (sound.attenuation() ? Attenuation.LINEAR : Attenuation.NONE)
                && this.sound.getDelay() == sound.delay()
                && this.sound.isRelative() == sound.relative();
    }

    public void setSound(@Nullable AmbientSound sound) {
        stop();

        if(sound == null) {
            this.sound = DEFAULT;
            return;
        }

        this.sound = new SimpleSoundInstance(sound.sound().getLocation(), sound.source(), sound.volume(), sound.pitch(), RandomSource.create(), sound.loop(), sound.delay(), sound.attenuation() ? Attenuation.LINEAR : Attenuation.NONE, this.pos.getX(), this.pos.getY(), this.pos.getZ(), sound.relative());
        play();
    }

    public boolean isPlaying() {
        return this.sound != DEFAULT && Minecraft.getInstance().getSoundManager().isActive(this.sound);
    }

    public void play() {
        if(this.sound != DEFAULT)
            Minecraft.getInstance().getSoundManager().play(sound);
    }

    public void stop() {
        if(this.sound != DEFAULT)
            Minecraft.getInstance().getSoundManager().stop(sound);
    }
}

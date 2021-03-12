package fr.frinn.custommachinery.common.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.registries.ForgeRegistries;

public class SoundManager {

    private BlockPos pos;
    private ISound sound;
    private boolean isPlaying = false;

    public SoundManager(BlockPos pos) {
        this.pos = pos;
    }

    public ResourceLocation getSound() {
        if(this.sound != null)
            return this.sound.getSoundLocation();
        else
            return null;
    }

    public void setSound(SoundEvent sound) {
        this.sound = new SimpleSound(sound, SoundCategory.BLOCKS, 1.0F, 1.0F, this.pos);
    }

    public boolean isPlaying() {
        if(this.sound != null)
            return Minecraft.getInstance().getSoundHandler().isPlaying(this.sound);
        else
            return false;
    }

    public void play() {
        if(this.sound != null)
            Minecraft.getInstance().getSoundHandler().play(this.sound);
    }

    public void stop() {
        if(this.sound != null)
            Minecraft.getInstance().getSoundHandler().stop(this.sound);
    }
}

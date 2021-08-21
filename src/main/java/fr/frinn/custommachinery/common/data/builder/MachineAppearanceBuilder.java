package fr.frinn.custommachinery.common.data.builder;

import fr.frinn.custommachinery.common.data.MachineAppearance;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public class MachineAppearanceBuilder {

    private ResourceLocation blockModel;
    private ResourceLocation itemModel;
    private SoundEvent sound;
    private int lightLevel;
    private int color;

    public MachineAppearanceBuilder() {
        this(MachineAppearance.DEFAULT);
    }

    public MachineAppearanceBuilder(MachineAppearance appearance) {
        this.blockModel = appearance.getBlockModel();
        this.itemModel = appearance.getItemModel();
        this.sound = appearance.getSound();
        this.lightLevel = appearance.getLightLevel();
        this.color = appearance.getColor();
    }

    public ResourceLocation getBlockModel() {
        return this.blockModel;
    }

    public void setBlockModel(ResourceLocation blockModel) {
        this.blockModel = blockModel;
    }

    public ResourceLocation getItemModel() {
        return this.itemModel;
    }

    public void setItemModel(ModelResourceLocation itemModel) {
        this.itemModel = itemModel;
    }

    public SoundEvent getSound() {
        return this.sound;
    }

    public void setSound(SoundEvent sound) {
        this.sound = sound;
    }

    public int getLightLevel() {
        return this.lightLevel;
    }

    public void setLightLevel(int lightLevel) {
        this.lightLevel = lightLevel;
    }

    public int getColor() {
        return this.color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public MachineAppearance build() {
        return new MachineAppearance(this.blockModel, this.itemModel, this.sound, this.lightLevel, this.color);
    }
}

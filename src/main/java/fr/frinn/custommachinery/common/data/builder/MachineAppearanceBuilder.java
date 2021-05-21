package fr.frinn.custommachinery.common.data.builder;

import fr.frinn.custommachinery.common.data.MachineAppearance;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public class MachineAppearanceBuilder {

    private MachineAppearance.AppearanceType type;
    private ResourceLocation model;
    private Block block;
    private ModelResourceLocation blockState;
    private ResourceLocation itemTexture;
    private SoundEvent sound;
    private MachineAppearance.LightMode lightMode;
    private int lightLevel;
    private int color;

    public MachineAppearanceBuilder() {
        this.type = MachineAppearance.AppearanceType.DEFAULT;
        this.model = MachineAppearance.DEFAULT_MODEL;
        this.block = MachineAppearance.DEFAULT_BLOCK;
        this.blockState = MachineAppearance.DEFAULT_BLOCKSTATE;
        this.itemTexture = MachineAppearance.DEFAULT_ITEM;
        this.sound = MachineAppearance.DEFAULT_SOUND;
        this.lightMode = MachineAppearance.DEFAULT_LIGHT_MODE;
        this.lightLevel = MachineAppearance.DEFAULT_LIGHT_LEVEL;
        this.color = MachineAppearance.DEFAULT_COLOR;
    }

    public MachineAppearanceBuilder(MachineAppearance appearance) {
        this.type = appearance.getType();
        this.model = appearance.getModel();
        this.block = appearance.getBlock();
        this.blockState = appearance.getBlockstate();
        this.itemTexture = appearance.getItemTexture();
        this.sound = appearance.getSound();
        this.lightMode = appearance.getLightMode();
        this.lightLevel = appearance.getLightLevel();
        this.color = appearance.getColor();
    }

    public MachineAppearance.AppearanceType getType() {
        return this.type == null ? MachineAppearance.AppearanceType.DEFAULT : this.type;
    }

    public void setType(MachineAppearance.AppearanceType type) {
        this.type = type;
    }

    public ResourceLocation getModel() {
        return this.model;
    }

    public void setModel(ResourceLocation model) {
        this.model = model;
    }

    public Block getBlock() {
        return this.block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public ModelResourceLocation getBlockState() {
        return this.blockState;
    }

    public void setBlockState(ModelResourceLocation blockState) {
        this.blockState = blockState;
    }

    public ResourceLocation getItemTexture() {
        return this.itemTexture;
    }

    public void setItemTexture(ResourceLocation itemTexture) {
        this.itemTexture = itemTexture;
    }

    public SoundEvent getSound() {
        return this.sound;
    }

    public void setSound(SoundEvent sound) {
        this.sound = sound;
    }

    public MachineAppearance.LightMode getLightMode() {
        return this.lightMode;
    }

    public void setLightMode(MachineAppearance.LightMode lightMode) {
        this.lightMode = lightMode;
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
        return new MachineAppearance(this.getType(), this.model, this.block, this.blockState, this.itemTexture, this.sound, this.lightMode, this.lightLevel, this.color);
    }
}

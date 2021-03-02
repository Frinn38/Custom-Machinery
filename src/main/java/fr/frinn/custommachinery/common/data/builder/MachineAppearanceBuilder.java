package fr.frinn.custommachinery.common.data.builder;

import fr.frinn.custommachinery.common.data.MachineAppearance;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;

public class MachineAppearanceBuilder {

    private MachineAppearance.AppearanceType type;
    private ResourceLocation model;
    private Block block;
    private ModelResourceLocation blockState;
    private ResourceLocation itemTexture;

    public MachineAppearanceBuilder() {

    }

    public MachineAppearanceBuilder(MachineAppearance appearance) {
        this.type = appearance.getType();
        this.model = appearance.getModel();
        this.block = appearance.getBlock();
        this.blockState = appearance.getBlockstate();
        this.itemTexture = appearance.getItemTexture();
    }

    public MachineAppearance.AppearanceType getType() {
        return this.type;
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

    public MachineAppearance build() {
        return new MachineAppearance(this.type, this.model, this.block, this.blockState, this.itemTexture);
    }
}

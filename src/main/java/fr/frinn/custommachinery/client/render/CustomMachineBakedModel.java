package fr.frinn.custommachinery.client.render;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.data.MachineAppearance;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class CustomMachineBakedModel implements IBakedModel {

    public static final ModelProperty<MachineAppearance> APPEARANCE = new ModelProperty<>();
    public static final CustomMachineBakedModel INSTANCE = new CustomMachineBakedModel();
    public static final ResourceLocation DEFAULT_MODEL = new ResourceLocation(CustomMachinery.MODID, "block/custom_machine_block");

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        return this.getQuads(state, side, rand, EmptyModelData.INSTANCE);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isSideLit() {
        return true;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.getParticleTexture(EmptyModelData.INSTANCE);
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData data) {
        Direction direction = side;
        if(state != null && direction != null)
            direction = MachineRenderer.INSTANCE.getRotatedDirection(state.get(BlockStateProperties.HORIZONTAL_FACING), direction);
        if(!data.hasProperty(APPEARANCE) || data.getData(APPEARANCE) == null)
            return Minecraft.getInstance().getModelManager().getMissingModel().getQuads(state, direction, rand, data);
        IBakedModel model = getMachineModel(data.getData(APPEARANCE));
        return model.getQuads(state, direction, rand, data);
    }

    @Override
    public TextureAtlasSprite getParticleTexture(@Nonnull IModelData data) {
        if(!data.hasProperty(APPEARANCE) || data.getData(APPEARANCE) == null)
            return Minecraft.getInstance().getModelManager().getMissingModel().getParticleTexture(data);
        IBakedModel model = getMachineModel(data.getData(APPEARANCE));
        return model.getParticleTexture(data);
    }

    public IBakedModel getMachineModel(MachineAppearance appearance) {
        IBakedModel machineModel;
        switch (appearance.getType()) {
            case BLOCK:
                machineModel =  Minecraft.getInstance().getModelManager().getModel(BlockModelShapes.getModelLocation(appearance.getBlock().getDefaultState()));
                break;
            case BLOCKSTATE:
                machineModel =  Minecraft.getInstance().getModelManager().getModel(appearance.getBlockstate());
                break;
            case MODEL:
                machineModel = Minecraft.getInstance().getModelManager().getModel(appearance.getModel());
                break;
            default:
                machineModel = Minecraft.getInstance().getModelManager().getModel(DEFAULT_MODEL);
                break;
        }
        if(machineModel == Minecraft.getInstance().getModelManager().getMissingModel())
            machineModel = Minecraft.getInstance().getModelManager().getModel(DEFAULT_MODEL);

        return machineModel;
    }
}

package fr.frinn.custommachinery.client.render;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.data.MachineAppearance;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

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
        if(!data.hasProperty(APPEARANCE) || data.getData(APPEARANCE) == null)
            return Minecraft.getInstance().getModelManager().getMissingModel().getQuads(state, side, rand, EmptyModelData.INSTANCE);
        IBakedModel model = getMachineModel(data.getData(APPEARANCE));
        if(state != null && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
            model = getRotatedModel(model, state.get(BlockStateProperties.HORIZONTAL_FACING), rand);
        return model.getQuads(state, side, rand, EmptyModelData.INSTANCE);
    }

    //TODO : Fix south rendering
    private IBakedModel getRotatedModel(IBakedModel model, Direction machineFacing, Random random) {
        List<BakedQuad> generalQuads = getQuadsRotated(model.getQuads(null, null, random, EmptyModelData.INSTANCE), machineFacing, null);
        Map<Direction, List<BakedQuad>> faceQuads = new HashMap<>();
        for(Direction side : Direction.values()) {
            Direction rotated = getRotatedDirection(machineFacing, side);
            faceQuads.put(rotated, getQuadsRotated(model.getQuads(null, side, random, EmptyModelData.INSTANCE), machineFacing, rotated));
        }
        return new SimpleBakedModel(generalQuads, faceQuads, model.isAmbientOcclusion(), model.isSideLit(), model.isGui3d(), model.getParticleTexture(EmptyModelData.INSTANCE), model.getItemCameraTransforms(), model.getOverrides());
    }

    private List<BakedQuad> getQuadsRotated(List<BakedQuad> quads, Direction machineFacing, Direction side) {
        return quads.stream().map(quad -> {
            Direction finalSide = side == null ? quad.getFace() : side;
            switch (machineFacing) {
                case SOUTH:
                    return rotateQuad(quad, Vector3f.YN.rotationDegrees(180), finalSide);
                case EAST:
                    return rotateQuad(quad, Vector3f.YN.rotationDegrees(90), finalSide);
                case WEST:
                    return rotateQuad(quad, Vector3f.YN.rotationDegrees(270), finalSide);
                default:
                    return rotateQuad(quad, Vector3f.YN.rotationDegrees(0), finalSide);
            }
        }).collect(Collectors.toList());
    }

    private BakedQuad rotateQuad(BakedQuad quad, Quaternion rotation, Direction side) {
        int[] quadData = quad.getVertexData();
        int[] newQuadData = Arrays.copyOf(quadData, quadData.length);
        for(int i = 0; i < quadData.length / 8; i++) {
            float x = Float.intBitsToFloat(quadData[i * 8]);
            float y = Float.intBitsToFloat(quadData[i * 8 + 1]);
            float z = Float.intBitsToFloat(quadData[i * 8 + 2]);
            Vector4f pos = new Vector4f(x - 0.5F, y - 0.5F, z - 0.5F, 1.0F);
            pos.transform(rotation);
            newQuadData[i * 8] = Float.floatToRawIntBits(pos.getX() + 0.5F);
            newQuadData[i * 8 + 1] = Float.floatToRawIntBits(pos.getY() + 0.5F);
            newQuadData[i * 8 + 2] = Float.floatToRawIntBits(pos.getZ() + 0.5F);
        }
        return new BakedQuad(newQuadData, quad.getTintIndex(), side, quad.getSprite(), quad.applyDiffuseLighting());
    }

    public Direction getRotatedDirection(Direction machineFacing, Direction quad) {
        if(quad.getAxis() == Direction.Axis.Y)
            return quad;

        switch(machineFacing) {
            case SOUTH:
                return Direction.byHorizontalIndex((quad.getHorizontalIndex() + 2) % 4);
            case WEST:
                return Direction.byHorizontalIndex((quad.getHorizontalIndex() + 3) % 4);
            case EAST:
                return Direction.byHorizontalIndex((quad.getHorizontalIndex() + 1) % 4);
            default:
                return quad;
        }
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

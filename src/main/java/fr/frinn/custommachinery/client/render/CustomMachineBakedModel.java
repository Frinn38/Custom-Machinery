package fr.frinn.custommachinery.client.render;

import com.google.common.collect.Maps;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.common.data.MachineAppearance;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class CustomMachineBakedModel implements IDynamicBakedModel {

    public static final ModelProperty<MachineAppearance> APPEARANCE = new ModelProperty<>();
    public static final ModelProperty<MachineStatus> STATUS = new ModelProperty<>();

    private final CustomMachineOverrideList overrideList = new CustomMachineOverrideList();
    private final Map<MachineStatus, ResourceLocation> defaults;

    public CustomMachineBakedModel(Map<MachineStatus, ResourceLocation> defaults) {
        this.defaults = defaults;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
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
        return this.overrideList;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData data) {
        IBakedModel model = getMachineBlockModel(data);
        if(state != null && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
            return getRotatedQuadsTest(model, state.get(BlockStateProperties.HORIZONTAL_FACING), side, rand);
        return model.getQuads(state, side, rand, EmptyModelData.INSTANCE);
    }

    private List<BakedQuad> getRotatedQuadsTest(IBakedModel model, Direction machineFacing, Direction side, Random random) {
        //side of the model before rotation
        Direction originalSide = getRotatedDirection(machineFacing, side);
        List<BakedQuad> finalQuads = model.getQuads(null, originalSide, random, EmptyModelData.INSTANCE);
        return finalQuads.stream().map(quad -> rotateQuad(quad, getRotation(machineFacing), side == null ? quad.getFace() : side)).collect(Collectors.toList());
    }

    private Quaternion getRotation(Direction machineFacing) {
        switch (machineFacing) {
            case EAST:
                return Vector3f.YN.rotationDegrees(90);
            case SOUTH:
                return Vector3f.YN.rotationDegrees(180);
            case WEST:
                return Vector3f.YN.rotationDegrees(270);
            default:
                return Quaternion.ONE;
        }
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
            pos.perspectiveDivide();
            newQuadData[i * 8] = Float.floatToRawIntBits(pos.getX() + 0.5F);
            newQuadData[i * 8 + 1] = Float.floatToRawIntBits(pos.getY() + 0.5F);
            newQuadData[i * 8 + 2] = Float.floatToRawIntBits(pos.getZ() + 0.5F);
        }
        return new BakedQuad(newQuadData, quad.getTintIndex(), side, quad.getSprite(), quad.applyDiffuseLighting());
    }

    public Direction getRotatedDirection(Direction machineFacing, @Nullable Direction quad) {
        if(quad == null || quad.getAxis() == Direction.Axis.Y)
            return quad;

        switch(machineFacing) {
            case WEST:
                return Direction.byHorizontalIndex((quad.getHorizontalIndex() + 1) % 4);
            case SOUTH:
                return Direction.byHorizontalIndex((quad.getHorizontalIndex() + 2) % 4);
            case EAST:
                return Direction.byHorizontalIndex((quad.getHorizontalIndex() + 3) % 4);
            default:
                return quad;
        }
    }

    @Override
    public TextureAtlasSprite getParticleTexture(@Nonnull IModelData data) {
        return getMachineBlockModel(data).getParticleTexture(data);
    }

    public IBakedModel getMachineBlockModel(@Nullable IModelData data) {
        if(data == null)
            return Minecraft.getInstance().getModelManager().getMissingModel();
        IBakedModel model = Minecraft.getInstance().getModelManager().getMissingModel();
        if(data.hasProperty(APPEARANCE) && data.getData(APPEARANCE) != null)
            model = Minecraft.getInstance().getModelManager().getModel(data.getData(APPEARANCE).getBlockModel());
        if(model == Minecraft.getInstance().getModelManager().getMissingModel() && data.hasProperty(STATUS) && data.getData(STATUS) != null)
            model = Minecraft.getInstance().getModelManager().getModel(this.defaults.get(data.getData(STATUS)));
        return model;
    }

    public IBakedModel getMachineItemModel(@Nullable MachineAppearance appearance) {
        IBakedModel model = Minecraft.getInstance().getModelManager().getMissingModel();

        if(appearance != null) {
            Item item = ForgeRegistries.ITEMS.getValue(appearance.getItemModel());
            if(item != null && Minecraft.getInstance().getItemRenderer().getItemModelMesher().getItemModel(item) != null)
                model = Minecraft.getInstance().getItemRenderer().getItemModelMesher().getItemModel(item);

            if(model == Minecraft.getInstance().getModelManager().getMissingModel())
                model = Minecraft.getInstance().getModelManager().getModel(appearance.getItemModel());
        }


        if(model == Minecraft.getInstance().getModelManager().getMissingModel())
            model = Minecraft.getInstance().getModelManager().getModel(this.defaults.get(MachineStatus.IDLE));

        return model;
    }
}

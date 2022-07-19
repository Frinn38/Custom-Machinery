package fr.frinn.custommachinery.client.render;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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

public class CustomMachineBakedModel implements IDynamicBakedModel {

    public static final ModelProperty<MachineAppearance> APPEARANCE = new ModelProperty<>();
    public static final ModelProperty<MachineStatus> STATUS = new ModelProperty<>();

    private final CustomMachineOverrideList overrideList = new CustomMachineOverrideList();
    private final Map<MachineStatus, ResourceLocation> defaults;

    public CustomMachineBakedModel(Map<MachineStatus, ResourceLocation> defaults) {
        this.defaults = defaults;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.getParticleIcon(EmptyModelData.INSTANCE);
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.overrideList;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData data) {
        BakedModel model = getMachineModel(data);
        if(state != null && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
            return getRotatedQuadsTest(model, state.getValue(BlockStateProperties.HORIZONTAL_FACING), side, rand);
        return model.getQuads(state, side, rand, EmptyModelData.INSTANCE);
    }

    private List<BakedQuad> getRotatedQuadsTest(BakedModel model, Direction machineFacing, Direction side, Random random) {
        //side of the model before rotation
        Direction originalSide = getRotatedDirection(machineFacing, side);
        List<BakedQuad> finalQuads = model.getQuads(null, originalSide, random, EmptyModelData.INSTANCE);
        return finalQuads.stream().map(quad -> rotateQuad(quad, getRotation(machineFacing), side == null ? quad.getDirection() : side)).toList();
    }

    private Quaternion getRotation(Direction machineFacing) {
        return switch (machineFacing) {
            case EAST -> Vector3f.YN.rotationDegrees(90);
            case SOUTH -> Vector3f.YN.rotationDegrees(180);
            case WEST -> Vector3f.YN.rotationDegrees(270);
            default -> Quaternion.ONE;
        };
    }

    private BakedQuad rotateQuad(BakedQuad quad, Quaternion rotation, Direction side) {
        int[] quadData = quad.getVertices();
        int[] newQuadData = Arrays.copyOf(quadData, quadData.length);
        for(int i = 0; i < quadData.length / 8; i++) {
            float x = Float.intBitsToFloat(quadData[i * 8]);
            float y = Float.intBitsToFloat(quadData[i * 8 + 1]);
            float z = Float.intBitsToFloat(quadData[i * 8 + 2]);
            Vector4f pos = new Vector4f(x - 0.5F, y - 0.5F, z - 0.5F, 1.0F);
            pos.transform(rotation);
            pos.perspectiveDivide();
            newQuadData[i * 8] = Float.floatToRawIntBits(pos.x() + 0.5F);
            newQuadData[i * 8 + 1] = Float.floatToRawIntBits(pos.y() + 0.5F);
            newQuadData[i * 8 + 2] = Float.floatToRawIntBits(pos.z() + 0.5F);
        }
        return new BakedQuad(newQuadData, quad.getTintIndex(), side, quad.getSprite(), quad.isShade());
    }

    public Direction getRotatedDirection(Direction machineFacing, @Nullable Direction quad) {
        if(quad == null || quad.getAxis() == Direction.Axis.Y)
            return quad;

        switch(machineFacing) {
            case WEST:
                return Direction.from2DDataValue((quad.get2DDataValue() + 1) % 4);
            case SOUTH:
                return Direction.from2DDataValue((quad.get2DDataValue() + 2) % 4);
            case EAST:
                return Direction.from2DDataValue((quad.get2DDataValue() + 3) % 4);
            default:
                return quad;
        }
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@Nonnull IModelData data) {
        return getMachineModel(data).getParticleIcon(data);
    }

    private BakedModel getMachineModel(@Nonnull IModelData data) {
        MachineAppearance appearance = data.getData(APPEARANCE);
        MachineStatus status = data.getData(STATUS);
        BakedModel model;
        if(appearance != null)
            model = getMachineBlockModel(appearance, data.getData(STATUS));
        else if(data.getData(STATUS) != null)
            model = Minecraft.getInstance().getModelManager().getModel(this.defaults.get(status));
        else
            model = Minecraft.getInstance().getModelManager().getModel(this.defaults.get(MachineStatus.IDLE));
        return model;
    }

    public BakedModel getMachineBlockModel(MachineAppearance appearance, @Nullable MachineStatus status) {
        BakedModel missing = Minecraft.getInstance().getModelManager().getMissingModel();
        BakedModel model;
        ResourceLocation blockModelLocation = appearance.getBlockModel();
        if(blockModelLocation instanceof ModelResourceLocation)
            model = Minecraft.getInstance().getModelManager().getModel(blockModelLocation);
        else {
            Block block = ForgeRegistries.BLOCKS.getValue(blockModelLocation);
            if(block != null && block != Blocks.AIR)
                model = Minecraft.getInstance().getModelManager().getModel(BlockModelShaper.stateToModelLocation(block.defaultBlockState()));
            else
                model = Minecraft.getInstance().getModelManager().getModel(blockModelLocation);
        }
        if(model == missing)
            model = Minecraft.getInstance().getModelManager().getModel(this.defaults.get(status == null ? MachineStatus.IDLE : status));

        return model;
    }

    public BakedModel getMachineItemModel(@Nullable MachineAppearance appearance) {
        BakedModel missing = Minecraft.getInstance().getModelManager().getMissingModel();
        BakedModel model = missing;

        if(appearance != null) {
            Item item = ForgeRegistries.ITEMS.getValue(appearance.getItemModel());
            if(item != null && Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(item) != null)
                model = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(item);

            if(model == missing)
                model = Minecraft.getInstance().getModelManager().getModel(appearance.getItemModel());

            if(model == Minecraft.getInstance().getModelManager().getModel(this.defaults.get(MachineStatus.IDLE)) || model == missing) {
                Item item2 = ForgeRegistries.ITEMS.getValue(appearance.getBlockModel());
                if(item2 != null && Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(item2) != null)
                    model = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(item2);

                if(model == missing)
                    model = getMachineBlockModel(appearance, MachineStatus.IDLE);
            }
        }

        if(model == missing)
            model = Minecraft.getInstance().getModelManager().getModel(this.defaults.get(MachineStatus.IDLE));

        return model;
    }
}

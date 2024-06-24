package fr.frinn.custommachinery.client.model;

import fr.frinn.custommachinery.api.machine.IMachineAppearance;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.common.init.CustomMachineItem;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import fr.frinn.custommachinery.impl.util.IMachineModelLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.common.NeoForgeConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
        return NeoForgeConfig.CLIENT.experimentalForgeLightPipelineEnabled.get();
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

    @SuppressWarnings("deprecation")
    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.getParticleIcon(ModelData.EMPTY);
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.overrideList;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        return getMachineModel(data).getRenderTypes(state, rand, data);
    }

    @Override
    public List<RenderType> getRenderTypes(ItemStack stack, boolean fabulous) {
        return CustomMachineItem.getMachine(stack)
                .map(machine -> getMachineItemModel(machine.getAppearance(MachineStatus.IDLE)).getRenderTypes(stack, fabulous))
                .orElse(List.of(RenderTypeHelper.getFallbackItemRenderType(stack, this, fabulous)));
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, RenderType type) {
        BakedModel model = getMachineModel(data);
        if(state != null && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return getRotatedQuads(model, state.getValue(BlockStateProperties.HORIZONTAL_FACING), side, rand, type);
        }
        return model.getQuads(state, side, rand, ModelData.EMPTY, type);
    }

    private List<BakedQuad> getRotatedQuads(BakedModel model, Direction machineFacing, Direction side, RandomSource random, RenderType type) {
        //side of the model before rotation
        Direction originalSide = getRotatedDirection(machineFacing, side);
        List<BakedQuad> finalQuads = model.getQuads(null, originalSide, random, ModelData.EMPTY, type);
        return finalQuads.stream().map(quad -> rotateQuad(quad, getRotation(machineFacing), side == null ? quad.getDirection() : side)).toList();
    }

    private Quaternionf getRotation(Direction machineFacing) {
        return switch (machineFacing) {
            case EAST -> new Quaternionf().fromAxisAngleDeg(0, -1, 0, 90);
            case SOUTH -> new Quaternionf().fromAxisAngleDeg(0, -1, 0, 180);
            case WEST -> new Quaternionf().fromAxisAngleDeg(0, -1, 0, 270);
            default -> new Quaternionf();
        };
    }

    private BakedQuad rotateQuad(BakedQuad quad, Quaternionf rotation, Direction side) {
        int[] quadData = quad.getVertices();
        int[] newQuadData = Arrays.copyOf(quadData, quadData.length);
        for(int i = 0; i < quadData.length / 8; i++) {
            float x = Float.intBitsToFloat(quadData[i * 8]);
            float y = Float.intBitsToFloat(quadData[i * 8 + 1]);
            float z = Float.intBitsToFloat(quadData[i * 8 + 2]);
            Vector4f pos = new Vector4f(x - 0.5F, y - 0.5F, z - 0.5F, 1.0F);
            pos.rotate(rotation);
            pos.div(pos.w);
            newQuadData[i * 8] = Float.floatToRawIntBits(pos.x() + 0.5F);
            newQuadData[i * 8 + 1] = Float.floatToRawIntBits(pos.y() + 0.5F);
            newQuadData[i * 8 + 2] = Float.floatToRawIntBits(pos.z() + 0.5F);

            //Wipe normal's data, don't know why, but it works better without that.
            newQuadData[i * 8 + 7] = 0;
        }
        return new BakedQuad(newQuadData, quad.getTintIndex(), side, quad.getSprite(), quad.isShade());
    }

    public Direction getRotatedDirection(Direction machineFacing, @Nullable Direction quad) {
        if(quad == null || quad.getAxis() == Direction.Axis.Y)
            return quad;

        return switch (machineFacing) {
            case WEST -> Direction.from2DDataValue((quad.get2DDataValue() + 1) % 4);
            case SOUTH -> Direction.from2DDataValue((quad.get2DDataValue() + 2) % 4);
            case EAST -> Direction.from2DDataValue((quad.get2DDataValue() + 3) % 4);
            default -> quad;
        };
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        return getMachineModel(data).getParticleIcon(data);
    }

    @SuppressWarnings("deprecation")
    @Override
    public ItemTransforms getTransforms() {
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(Blocks.STONE.defaultBlockState()).getTransforms();
    }

    private BakedModel getMachineModel(@NotNull ModelData data) {
        MachineAppearance appearance = data.get(APPEARANCE);
        MachineStatus status = data.get(STATUS);
        BakedModel model;
        if(appearance != null)
            model = getMachineBlockModel(appearance, data.get(STATUS));
        else if(data.get(STATUS) != null)
            model = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(this.defaults.get(status)));
        else
            model = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(this.defaults.get(MachineStatus.IDLE)));
        return model;
    }

    public BakedModel getMachineBlockModel(IMachineAppearance appearance, @Nullable MachineStatus status) {
        BakedModel missing = Minecraft.getInstance().getModelManager().getMissingModel();
        BakedModel model = missing;
        IMachineModelLocation blockModelLocation = appearance.getBlockModel();

        if(blockModelLocation.getState() != null)
            model = Minecraft.getInstance().getBlockRenderer().getBlockModel(blockModelLocation.getState());
        else if(blockModelLocation.getLoc() != null && blockModelLocation.getProperties() != null)
            model = Minecraft.getInstance().getModelManager().getModel(new ModelResourceLocation(blockModelLocation.getLoc(), blockModelLocation.getProperties()));
        else if(blockModelLocation.getLoc() != null)
            model = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(blockModelLocation.getLoc()));

        if(model == missing)
            model = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(this.defaults.get(status == null ? MachineStatus.IDLE : status)));

        return model;
    }

    public BakedModel getMachineItemModel(@Nullable IMachineAppearance appearance) {
        BakedModel missing = Minecraft.getInstance().getModelManager().getMissingModel();
        BakedModel model = missing;

        if(appearance != null) {
            IMachineModelLocation itemModelLocation = appearance.getItemModel();
            if(itemModelLocation.getState() != null && itemModelLocation.getState().getBlock().asItem() != Items.AIR)
                model = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(itemModelLocation.getState().getBlock().asItem());
            else if(itemModelLocation.getLoc() != null) {
                Item item = BuiltInRegistries.ITEM.get(itemModelLocation.getLoc());
                if(itemModelLocation.getProperties() != null)
                    model = Minecraft.getInstance().getModelManager().getModel(new ModelResourceLocation(itemModelLocation.getLoc(), itemModelLocation.getProperties()));
                else if(item != Items.AIR && Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(item) != null)
                    model = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(item);
                else
                    model = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(itemModelLocation.getLoc()));
            }

            if(model == missing)
                model = getMachineBlockModel(appearance, MachineStatus.IDLE);
        }

        if(model == missing)
            model = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(this.defaults.get(MachineStatus.IDLE)));

        return model;
    }
}

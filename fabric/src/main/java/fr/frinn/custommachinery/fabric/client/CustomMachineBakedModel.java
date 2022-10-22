package fr.frinn.custommachinery.fabric.client;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import fr.frinn.custommachinery.api.machine.IMachineAppearance;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import fr.frinn.custommachinery.impl.util.ModelLocation;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

public class CustomMachineBakedModel implements BakedModel, FabricBakedModel {

    private final Map<MachineStatus, ResourceLocation> defaults;

    public CustomMachineBakedModel(Map<MachineStatus, ResourceLocation> defaults) {
        this.defaults = defaults;
    }

    /** FABRIC STUFF **/

    @Override
    public boolean isVanillaAdapter() {
        return true;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        Optional.ofNullable(blockView.getBlockEntity(pos))
                .filter(be -> be instanceof MachineTile)
                .map(be -> ((MachineTile)be))
                .ifPresentOrElse(
                        machine -> {
                            context.pushTransform(QuadRotator.fromDirection(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
                            context.fallbackConsumer().accept(getMachineBlockModel(machine.getAppearance(), machine.getStatus()));
                            context.popTransform();
                        },
                        () -> context.fallbackConsumer().accept(this)
                );
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        context.fallbackConsumer().accept(this);
    }

    /** VANILLA STUFF **/

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, Random random) {
        return getModel(this.defaults.get(MachineStatus.IDLE)).getQuads(blockState, direction, random);
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
        return getModel(this.defaults.get(MachineStatus.IDLE)).getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public ItemOverrides getOverrides() {
        return CustomMachineOverrideList.INSTANCE;
    }

    /** CM STUFF **/

    public BakedModel getMachineBlockModel(IMachineAppearance appearance, @Nullable MachineStatus status) {
        BakedModel missing = Minecraft.getInstance().getModelManager().getMissingModel();
        BakedModel model;
        ModelLocation blockModelLocation = appearance.getBlockModel();
        if(blockModelLocation.getProperties() != null)
            model = getModel(new ModelResourceLocation(blockModelLocation.toString()));
        else {
            Block block = Registry.BLOCK.get(blockModelLocation.getLoc());
            if(block != Blocks.AIR)
                model = Minecraft.getInstance().getModelManager().getModel(BlockModelShaper.stateToModelLocation(block.defaultBlockState()));
            else
                model = getModel(blockModelLocation.getLoc());
        }
        if(model == missing)
            model = getModel(this.defaults.get(status == null ? MachineStatus.IDLE : status));

        return model;
    }

    public BakedModel getMachineItemModel(@Nullable MachineAppearance appearance) {
        BakedModel missing = Minecraft.getInstance().getModelManager().getMissingModel();
        BakedModel model = missing;

        if(appearance != null) {
            Item item = Registry.ITEM.get(appearance.getItemModel().getLoc());
            if(item != Items.AIR && Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(item) != null)
                model = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(item);

            if(model == missing)
                model = getModel(new ModelResourceLocation(appearance.getItemModel().toString()));

            if(model == getModel(this.defaults.get(MachineStatus.IDLE)) || model == missing) {
                Item item2 = Registry.ITEM.get(appearance.getBlockModel().getLoc());
                if(item2 != Items.AIR && Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(item2) != null)
                    model = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(item2);

                if(model == missing)
                    model = getMachineBlockModel(appearance, MachineStatus.IDLE);
            }
        }

        if(model == missing)
            model = getModel(this.defaults.get(MachineStatus.IDLE));

        return model;
    }

    private BakedModel getModel(ResourceLocation loc) {
        return Minecraft.getInstance().getModelManager().bakedRegistry.getOrDefault(loc, Minecraft.getInstance().getModelManager().getMissingModel());
    }

    private enum QuadRotator implements RenderContext.QuadTransform {
        NORTH(Direction.NORTH, Vector3f.YN.rotationDegrees(0)),
        SOUTH(Direction.SOUTH, Vector3f.YN.rotationDegrees(180)),
        EAST(Direction.EAST, Vector3f.YN.rotationDegrees(90)),
        WEST(Direction.WEST, Vector3f.YN.rotationDegrees(270));

        public static QuadRotator fromDirection(Direction direction) {
            return switch (direction) {
                case SOUTH -> SOUTH;
                case EAST -> EAST;
                case WEST -> WEST;
                default -> NORTH;
            };
        }

        private final Direction facing;
        private final Quaternion rotation;

        QuadRotator(Direction facing, Quaternion rotation) {
            this.facing = facing;
            this.rotation = rotation;
        }

        @Override
        public boolean transform(MutableQuadView quad) {
            for(int index = 0; index < 4; index++) {
                Vector3f vec = quad.copyPos(index, null);
                vec.add(-0.5F, -0.5F, -0.5F);
                vec.transform(this.rotation);
                vec.add(0.5F, 0.5F, 0.5F);
                quad.pos(index, vec);

                Vector3f normal = quad.copyNormal(index, null);
                if(normal != null) {
                    normal.add(-0.5F, -0.5F, -0.5F);
                    normal.transform(this.rotation);
                    normal.normalize();
                    normal.add(0.5F, 0.5F, 0.5F);
                    quad.normal(index, normal);
                }
            }
            quad.cullFace(getRotatedDirection(this.facing, quad.cullFace()));
            return true;
        }

        public Direction getRotatedDirection(Direction machineFacing, @Nullable Direction quad) {
            if(quad == null || quad.getAxis() == Direction.Axis.Y)
                return quad;

            return switch (machineFacing) {
                case WEST -> Direction.from2DDataValue((quad.get2DDataValue() + 3) % 4);
                case SOUTH -> Direction.from2DDataValue((quad.get2DDataValue() + 2) % 4);
                case EAST -> Direction.from2DDataValue((quad.get2DDataValue() + 1) % 4);
                default -> quad;
            };
        }
    }
}

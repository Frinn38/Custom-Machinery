package fr.frinn.custommachinery.fabric.client;

import fr.frinn.custommachinery.api.machine.IMachineAppearance;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.impl.util.IMachineModelLocation;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.impl.renderer.VanillaModelEncoder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class CustomMachineBakedModel implements BakedModel, FabricBakedModel {

    private final Map<MachineStatus, ResourceLocation> defaults;

    public CustomMachineBakedModel(Map<MachineStatus, ResourceLocation> defaults) {
        this.defaults = defaults;
    }

    /** FABRIC STUFF **/

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        Optional.ofNullable(blockView.getBlockEntity(pos))
                .filter(be -> be instanceof MachineTile)
                .map(be -> ((MachineTile)be))
                .ifPresentOrElse(
                        machine -> {
                            context.pushTransform(QuadRotator.fromDirection(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
                            getMachineBlockModel(machine.getAppearance(), machine.getStatus()).emitBlockQuads(blockView, state, pos, randomSupplier, context);
                            context.popTransform();
                        },
                        () -> VanillaModelEncoder.emitBlockQuads(this, state, randomSupplier, context, context.getEmitter())
                );
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
        VanillaModelEncoder.emitItemQuads(this, null, randomSupplier, context);
    }

    /** VANILLA STUFF **/

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, RandomSource random) {
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
        BakedModel model = missing;
        IMachineModelLocation blockModelLocation = appearance.getBlockModel();

        if(blockModelLocation.getState() != null)
            model = Minecraft.getInstance().getBlockRenderer().getBlockModel(blockModelLocation.getState());
        else if(blockModelLocation.getLoc() != null && blockModelLocation.getProperties() != null)
            model = Minecraft.getInstance().getModelManager().getModel(new ModelResourceLocation(blockModelLocation.getLoc(), blockModelLocation.getProperties()));
        else if(blockModelLocation.getLoc() != null)
            model = getModel(blockModelLocation.getLoc());

        if(model == missing)
            model = getModel(this.defaults.get(status == null ? MachineStatus.IDLE : status));

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
                    model = getModel(itemModelLocation.getLoc());
            }

            if(model == missing)
                model = getMachineBlockModel(appearance, MachineStatus.IDLE);
        }

        if(model == missing)
            model = getModel(this.defaults.get(MachineStatus.IDLE));

        return model;
    }

    private BakedModel getModel(ResourceLocation loc) {
        return Minecraft.getInstance().getModelManager().bakedRegistry.getOrDefault(loc, Minecraft.getInstance().getModelManager().getMissingModel());
    }

    private enum QuadRotator implements RenderContext.QuadTransform {
        NORTH(Direction.NORTH, new Quaternionf().fromAxisAngleDeg(0, -1, 0, 0)),
        SOUTH(Direction.SOUTH, new Quaternionf().fromAxisAngleDeg(0, -1, 0, 180)),
        EAST(Direction.EAST, new Quaternionf().fromAxisAngleDeg(0, -1, 0, 90)),
        WEST(Direction.WEST, new Quaternionf().fromAxisAngleDeg(0, -1, 0, 270));

        public static QuadRotator fromDirection(Direction direction) {
            return switch (direction) {
                case SOUTH -> SOUTH;
                case EAST -> EAST;
                case WEST -> WEST;
                default -> NORTH;
            };
        }

        private final Direction facing;
        private final Quaternionf rotation;

        QuadRotator(Direction facing, Quaternionf rotation) {
            this.facing = facing;
            this.rotation = rotation;
        }

        @Override
        public boolean transform(MutableQuadView quad) {
            for(int index = 0; index < 4; index++) {
                Vector3f vec = quad.copyPos(index, null);
                vec.add(-0.5F, -0.5F, -0.5F);
                vec.rotate(this.rotation);
                vec.add(0.5F, 0.5F, 0.5F);
                quad.pos(index, vec);

                Vector3f normal = quad.copyNormal(index, null);
                if(normal != null) {
                    normal.add(-0.5F, -0.5F, -0.5F);
                    normal.rotate(this.rotation);
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

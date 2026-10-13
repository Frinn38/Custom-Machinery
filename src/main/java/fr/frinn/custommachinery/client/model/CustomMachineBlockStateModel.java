package fr.frinn.custommachinery.client.model;

import com.mojang.math.Axis;
import com.mojang.math.OctahedralGroup;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.machine.IMachineAppearance;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.util.MachineModelLocation.MachineBlockModelLocation;
import fr.frinn.custommachinery.impl.util.IMachineModelLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialFlags;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material.Baked;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public record CustomMachineBlockStateModel(BlockStateModelPart defaultNorth, BlockStateModelPart defaultEast, BlockStateModelPart defaultSouth, BlockStateModelPart defaultWest) implements DynamicBlockStateModel {

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        ModelData data = level.getModelData(pos);
        IMachineAppearance appearance = data.get(ClientHandler.APPEARANCE);
        if(appearance == null || appearance.getBlockModel() == MachineBlockModelLocation.DEFAULT) {
            BlockStateModelPart defaultModel = this.getDefaultModel(state);
            if(defaultModel != null)
                parts.add(defaultModel);
        } else {
            IMachineModelLocation.Block blockModel = appearance.getBlockModel();
            blockModel.location().ifLeft(mimicState -> {
                List<BlockStateModelPart> beforeRotation = new ArrayList<>();
                Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(mimicState).collectParts(level, pos, state, random, beforeRotation);
                List<BlockStateModelPart> rotatedParts = this.rotate(beforeRotation, state);
                parts.addAll(rotatedParts);
            });
            blockModel.location().ifRight(id -> {
                BlockStateModel model = ClientHandler.getStandaloneModel(id);
                if(model != null)
                    model.collectParts(level, pos, state, random, parts);
            });
        }
    }

    public List<BlockStateModelPart> rotate(List<BlockStateModelPart> parts, BlockState state) {
        Direction machineFacing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        if(machineFacing == Direction.NORTH || machineFacing.getAxis() == Direction.Axis.Y)
            return parts;

        Transformation transformation = switch (machineFacing) {
            case EAST -> new Transformation(new Matrix4f().rotationAround(Axis.YN.rotationDegrees(90), 0.5F, 0.5F, 0.5F));
            case SOUTH -> new Transformation(new Matrix4f().rotationAround(Axis.YN.rotationDegrees(180), 0.5F, 0.5F, 0.5F));
            case WEST -> new Transformation(new Matrix4f().rotationAround(Axis.YN.rotationDegrees(270), 0.5F, 0.5F, 0.5F));
            default -> Transformation.IDENTITY;
        };

        return parts.stream().map(part -> {
            QuadCollection.Builder builder = new QuadCollection.Builder();

            //Rotate face quads
            for(Direction originalSide : Direction.values()) {
                Direction finalSide = originalSide.getAxis() == Direction.Axis.Y ? originalSide : Direction.from2DDataValue((originalSide.get2DDataValue() + machineFacing.get2DDataValue() + 2) % 4);
                part.getQuads(originalSide).stream().map(quad -> QuadTransformer.applyTransformation(quad, transformation, finalSide)).forEach(quad -> builder.addCulledFace(finalSide, quad));
            }

            //Rotate global quads
            part.getQuads(null).stream().map(quad -> QuadTransformer.applyTransformation(quad, transformation, Direction.UP)).forEach(builder::addUnculledFace);

            return (BlockStateModelPart)new SimpleModelWrapper(builder.build(), !part.ambientOcclusion().isFalse(), part.particleMaterial());
        }).toList();
    }

    @Override
    public Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        ModelData data = level.getModelData(pos);
        IMachineAppearance appearance = data.get(ClientHandler.APPEARANCE);
        if(appearance == null) {
            return this.defaultNorth.particleMaterial();
        } else {
            IMachineModelLocation.Block blockModel = appearance.getBlockModel();
            return blockModel.location().map(
                    mimicState -> Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(mimicState).particleMaterial(level, pos, state),
                    id -> {
                        BlockStateModel model = ClientHandler.getStandaloneModel(id);
                        if(model != null)
                            return model.particleMaterial(level, pos, state);
                        else
                            return this.defaultNorth.particleMaterial();
                    }
            );
        }
    }

    @Override
    @MaterialFlags
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        ModelData data = level.getModelData(pos);
        IMachineAppearance appearance = data.get(ClientHandler.APPEARANCE);
        if(appearance == null) {
            return this.defaultNorth.materialFlags();
        } else {
            IMachineModelLocation.Block blockModel = appearance.getBlockModel();
            return blockModel.location().map(
                    mimicState -> Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(mimicState).materialFlags(level, pos, state),
                    id -> {
                        BlockStateModel model = ClientHandler.getStandaloneModel(id);
                        if(model != null)
                            return model.materialFlags(level, pos, state);
                        else
                            return this.defaultNorth.materialFlags();
                    }
            );
        }
    }

    @Nullable
    private BlockStateModelPart getDefaultModel(BlockState state) {
        return switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH -> this.defaultNorth;
            case EAST -> this.defaultEast;
            case SOUTH -> this.defaultSouth;
            case WEST -> this.defaultWest;
            default -> null;
        };
    }

    //Unused
    @Override
    public Baked particleMaterial() {
        return this.defaultNorth.particleMaterial();
    }

    //Unused
    @Override
    @MaterialFlags
    public int materialFlags() {
        return this.defaultNorth.materialFlags();
    }

    public record Unbaked(Identifier defaultModel) implements CustomUnbakedBlockStateModel {

        public static final Identifier ID = CustomMachinery.rl("custom_machine_model");
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(builder ->
                builder.group(
                        Identifier.CODEC.optionalFieldOf("default", CustomMachinery.rl("default/custom_machine_default")).forGetter(Unbaked::defaultModel)
                ).apply(builder, Unbaked::new)
        );

        @Override
        public MapCodec<Unbaked> codec() {
            return CODEC;
        }

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            return new CustomMachineBlockStateModel(
                    SimpleModelWrapper.bake(baker, this.defaultModel, BlockModelRotation.IDENTITY),
                    SimpleModelWrapper.bake(baker, this.defaultModel, BlockModelRotation.get(OctahedralGroup.BLOCK_ROT_Y_90)),
                    SimpleModelWrapper.bake(baker, this.defaultModel, BlockModelRotation.get(OctahedralGroup.BLOCK_ROT_Y_180)),
                    SimpleModelWrapper.bake(baker, this.defaultModel, BlockModelRotation.get(OctahedralGroup.BLOCK_ROT_Y_270))
            );
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(this.defaultModel);
        }
    }
}

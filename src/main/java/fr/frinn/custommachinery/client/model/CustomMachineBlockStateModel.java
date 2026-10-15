package fr.frinn.custommachinery.client.model;

import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.machine.IMachineAppearance;
import fr.frinn.custommachinery.api.machine.MachineStatus;
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
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public record CustomMachineBlockStateModel(BlockStateModelPart defaultIdle, BlockStateModelPart defaultRunning, BlockStateModelPart defaultErrored, BlockStateModelPart defaultPaused) implements DynamicBlockStateModel {

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        ModelData data = level.getModelData(pos);
        IMachineAppearance appearance = data.get(ClientHandler.APPEARANCE);
        if(appearance == null || appearance.getBlockModel() == MachineBlockModelLocation.DEFAULT) {
            MachineStatus status = data.get(ClientHandler.STATUS);
            if(status == null)
                status = MachineStatus.IDLE;
            BlockStateModelPart defaultModel = this.getDefaultModel(status);
            parts.addAll(this.rotate(List.of(defaultModel), state));
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
            return this.defaultIdle.particleMaterial();
        } else {
            IMachineModelLocation.Block blockModel = appearance.getBlockModel();
            return blockModel.location().map(
                    mimicState -> Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(mimicState).particleMaterial(level, pos, state),
                    id -> {
                        BlockStateModel model = ClientHandler.getStandaloneModel(id);
                        if(model != null)
                            return model.particleMaterial(level, pos, state);
                        else
                            return this.defaultIdle.particleMaterial();
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
            return this.defaultIdle.materialFlags();
        } else {
            IMachineModelLocation.Block blockModel = appearance.getBlockModel();
            return blockModel.location().map(
                    mimicState -> Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(mimicState).materialFlags(level, pos, state),
                    id -> {
                        BlockStateModel model = ClientHandler.getStandaloneModel(id);
                        if(model != null)
                            return model.materialFlags(level, pos, state);
                        else
                            return this.defaultIdle.materialFlags();
                    }
            );
        }
    }

    private BlockStateModelPart getDefaultModel(MachineStatus status) {
        return switch (status) {
            case IDLE -> this.defaultIdle;
            case RUNNING -> this.defaultRunning;
            case ERRORED -> this.defaultErrored;
            case PAUSED -> this.defaultPaused;
        };
    }

    //Unused
    @Override
    public Baked particleMaterial() {
        return this.defaultIdle.particleMaterial();
    }

    //Unused
    @Override
    @MaterialFlags
    public int materialFlags() {
        return this.defaultIdle.materialFlags();
    }

    public record Unbaked(Identifier idle, Identifier running, Identifier errored, Identifier paused) implements CustomUnbakedBlockStateModel {

        public static final Identifier ID = CustomMachinery.rl("custom_machine_model");
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(builder ->
                builder.group(
                        Identifier.CODEC.optionalFieldOf("idle", CustomMachinery.rl("default/custom_machine_default_idle")).forGetter(Unbaked::idle),
                        Identifier.CODEC.optionalFieldOf("running", CustomMachinery.rl("default/custom_machine_default_running")).forGetter(Unbaked::running),
                        Identifier.CODEC.optionalFieldOf("errored", CustomMachinery.rl("default/custom_machine_default_errored")).forGetter(Unbaked::errored),
                        Identifier.CODEC.optionalFieldOf("paused", CustomMachinery.rl("default/custom_machine_default_paused")).forGetter(Unbaked::paused)
                ).apply(builder, Unbaked::new)
        );

        @Override
        public MapCodec<Unbaked> codec() {
            return CODEC;
        }

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            return new CustomMachineBlockStateModel(
                    SimpleModelWrapper.bake(baker, this.idle, BlockModelRotation.IDENTITY),
                    SimpleModelWrapper.bake(baker, this.running, BlockModelRotation.IDENTITY),
                    SimpleModelWrapper.bake(baker, this.errored, BlockModelRotation.IDENTITY),
                    SimpleModelWrapper.bake(baker, this.paused, BlockModelRotation.IDENTITY)
            );
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(this.idle);
            resolver.markDependency(this.running);
            resolver.markDependency(this.errored);
            resolver.markDependency(this.paused);
        }
    }
}

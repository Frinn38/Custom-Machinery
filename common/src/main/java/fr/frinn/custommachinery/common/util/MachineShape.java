package fr.frinn.custommachinery.common.util;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.RecordBuilder;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MachineShape implements Function<Direction, VoxelShape> {

    private static final NamedCodec<List<AABB>> BOX_CODEC = DefaultCodecs.BOX.listOf();
    private static final NamedCodec<Map<Direction, List<AABB>>> MAP_CODEC = NamedCodec.unboundedMap(DefaultCodecs.DIRECTION, BOX_CODEC, "Map<Direction, List<Box>>");

    public static final NamedCodec<MachineShape> CODEC = new NamedCodec<>() {
        @Override
        public <T> DataResult<Pair<MachineShape, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<PartialBlockState> block = PartialBlockState.CODEC.read(ops, input);
            if(block.result().isPresent()) {
                BlockState state = block.result().get().getBlockState();
                Map<Direction, VoxelShape> shapes = Maps.newEnumMap(Direction.class);
                try {
                    for (Direction side : Direction.values()) {
                        shapes.put(side, state.getShape(null, null));
                    }
                } catch (Exception e) {
                    return DataResult.error("Can't mimic shape of block: " + block.result().get());
                }
                return DataResult.success(Pair.of(new MachineShape(shapes), ops.empty()));
            }
            DataResult<List<AABB>> boxes = BOX_CODEC.read(ops, input);
            if(boxes.result().isPresent()) {
                VoxelShape shape = fromAABBList(boxes.result().get());
                Map<Direction, VoxelShape> shapes = Maps.newEnumMap(Direction.class);
                for (Direction side : Direction.values()) {
                    if(side.getAxis() == Axis.Y)
                        continue;
                    shapes.put(side, rotateShape(Direction.NORTH, side, shape));
                }
                return DataResult.success(Pair.of(new MachineShape(shapes), ops.empty()));
            }
            DataResult<Map<Direction, List<AABB>>> map = MAP_CODEC.read(ops, input);
            if(map.result().isPresent()) {
                Map<Direction, VoxelShape> shapes = Maps.newEnumMap(Direction.class);
                map.result().get().forEach((side, box) -> {
                    shapes.put(side, fromAABBList(box));
                });
                return DataResult.success(Pair.of(new MachineShape(shapes), ops.empty()));
            }
            return DataResult.error("Can't parse block shape: " + input);
        }

        @Override
        public <T> DataResult<T> encode(DynamicOps<T> ops, MachineShape input, T prefix) {
            RecordBuilder<T> builder = ops.mapBuilder();
            input.shapes.forEach((side, shape) -> {
                builder.add(side.getName(), BOX_CODEC.encodeStart(ops, shape.toAabbs()));
            });
            return builder.build(prefix);
        }

        @Override
        public String name() {
            return "Machine Shape";
        }
    };

    public static final MachineShape DEFAULT = new MachineShape();
    public static final MachineShape DEFAULT_COLLISION = new MachineShape();

    private final Map<Direction, VoxelShape> shapes = Maps.newEnumMap(Direction.class);

    public MachineShape() {
        for (Direction side : Direction.values()) {
            this.shapes.put(side, Shapes.block());
        }
    }

    public MachineShape(Map<Direction, VoxelShape> shapes) {
        this.shapes.putAll(shapes);
    }

    @Override
    public VoxelShape apply(Direction side) {
        return this.shapes.get(side);
    }

    private static VoxelShape fromAABBList(List<AABB> list) {
        VoxelShape shape = Shapes.empty();
        for(AABB box : list) {
            VoxelShape partial = Shapes.create(box);
            shape = Shapes.joinUnoptimized(shape, partial, BooleanOp.OR);
        }
        return shape;
    }

    private static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
        if (from == to) { return shape; }

        VoxelShape[] buffer = new VoxelShape[] { shape, Shapes.empty() };

        int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;
        for (int i = 0; i < times; i++)
        {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = Shapes.or(
                    buffer[1],
                    Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)
            ));
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }

        return buffer[0];
    }
}

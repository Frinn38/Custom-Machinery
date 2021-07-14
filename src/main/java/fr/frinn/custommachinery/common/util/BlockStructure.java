package fr.frinn.custommachinery.common.util;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class BlockStructure {

    private final Map<BlockPos, PartialBlockState> blocks_north;
    private final Map<BlockPos, PartialBlockState> blocks_east;
    private final Map<BlockPos, PartialBlockState> blocks_south;
    private final Map<BlockPos, PartialBlockState> blocks_west;

    public BlockStructure(Map<BlockPos, PartialBlockState> blocks) {
        this.blocks_south = blocks;
        this.blocks_west = rotate(blocks, Rotation.CLOCKWISE_90);
        this.blocks_north = rotate(blocks, Rotation.CLOCKWISE_180);
        this.blocks_east = rotate(blocks, Rotation.COUNTERCLOCKWISE_90);
    }

    public Map<BlockPos, PartialBlockState> getBlocks(Direction direction) {
        switch (direction) {
            case SOUTH:
                return blocks_south;
            case EAST:
                return this.blocks_east;
            case WEST:
                return this.blocks_west;
            default:
                return this.blocks_north;
        }
    }

    public boolean match(IWorldReader world, BlockPos machinePos, Direction machineFacing) {
        Map<BlockPos, PartialBlockState> blocks = getBlocks(machineFacing);
        BlockPos.Mutable worldPos = new BlockPos.Mutable();
        for(BlockPos pos : blocks.keySet()) {
            PartialBlockState state = blocks.get(pos);
            worldPos.setPos(pos.getX() + machinePos.getX(), pos.getY() + machinePos.getY(), pos.getZ() + machinePos.getZ());
            CachedBlockInfo info = new CachedBlockInfo(world, worldPos, false);
            if(!state.test(info))
                return false;
        }
        return true;
    }

    private Map<BlockPos, PartialBlockState> rotate(Map<BlockPos, PartialBlockState> blocks, Rotation rotation) {
        Map<BlockPos, PartialBlockState> rotated = new HashMap<>();
        blocks.forEach((pos, state) -> rotated.put(pos.rotate(rotation), state));
        return rotated;
    }

    public static class Builder {

        private static final Joiner COMMA_JOIN = Joiner.on(",");
        private final List<String[]> depth = Lists.newArrayList();
        private final Map<Character, PartialBlockState> symbolMap = Maps.newHashMap();
        private int aisleHeight;
        private int rowWidth;

        private Builder() {
            this.symbolMap.put(' ', PartialBlockState.ANY);
            this.symbolMap.put('m', new PartialBlockState(Registration.CUSTOM_MACHINE_BLOCK.get().getDefaultState(), new HashSet<>(), null));
        }

        /**
         * Adds a single aisle to this pattern, going in the z axis. (so multiple calls to this will increase the z-size by
         * 1)
         */
        public Builder aisle(String... aisle) {
            if (!ArrayUtils.isEmpty(aisle) && !StringUtils.isEmpty(aisle[0])) {
                if (this.depth.isEmpty()) {
                    this.aisleHeight = aisle.length;
                    this.rowWidth = aisle[0].length();
                }

                if (aisle.length != this.aisleHeight) {
                    throw new IllegalArgumentException("Expected aisle with height of " + this.aisleHeight + ", but was given one with a height of " + aisle.length + ")");
                } else {
                    for(String s : aisle) {
                        if (s.length() != this.rowWidth) {
                            throw new IllegalArgumentException("Not all rows in the given aisle are the correct width (expected " + this.rowWidth + ", found one with " + s.length() + ")");
                        }

                        for(char c0 : s.toCharArray()) {
                            if (!this.symbolMap.containsKey(c0)) {
                                this.symbolMap.put(c0, null);
                            }
                        }
                    }

                    this.depth.add(aisle);
                    return this;
                }
            } else {
                throw new IllegalArgumentException("Empty pattern for aisle");
            }
        }

        public static Builder start() {
            return new Builder();
        }

        public Builder where(char symbol, PartialBlockState blockMatcher) {
            this.symbolMap.put(symbol, blockMatcher);
            return this;
        }

        public BlockStructure build() {
            this.checkMissingPredicates();
            BlockPos machinePos = this.getMachinePos();
            Map<BlockPos, PartialBlockState> blocks = new HashMap<>();
            for(int i = 0; i < this.depth.size(); ++i) {
                for(int j = 0; j < this.aisleHeight; ++j) {
                    for(int k = 0; k < this.rowWidth; ++k) {
                        blocks.put(new BlockPos(k - machinePos.getX(), i - machinePos.getY(), j - machinePos.getZ()), this.symbolMap.get((this.depth.get(i))[j].charAt(k)));
                    }
                }
            }
            return new BlockStructure(blocks);
        }

        private BlockPos getMachinePos() {
            BlockPos machinePos = null;
            for(int i = 0; i < this.depth.size(); ++i) {
                for(int j = 0; j < this.aisleHeight; ++j) {
                    for(int k = 0; k < this.rowWidth; ++k) {
                       if((this.depth.get(i))[j].charAt(k) == 'm')
                           if(machinePos == null)
                               machinePos = new BlockPos(k, i, j);
                           else
                               throw new IllegalStateException("The structure pattern need exactly one 'm' character to defined the machine position, several found !");
                    }
                }
            }
            if(machinePos != null)
                return machinePos;
            throw new IllegalStateException("You need to define the machine position in the structure with character 'm'");
        }

        private void checkMissingPredicates() {
            List<Character> list = Lists.newArrayList();

            for(Map.Entry<Character, PartialBlockState> entry : this.symbolMap.entrySet()) {
                if (entry.getValue() == null) {
                    list.add(entry.getKey());
                }
            }

            if (!list.isEmpty()) {
                throw new IllegalStateException("Blocks for character(s) " + COMMA_JOIN.join(list) + " are missing");
            }
        }
    }
}

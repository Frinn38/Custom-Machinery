package fr.frinn.custommachinery.common.util;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockStructure {

    private final Map<BlockPos, List<BlockIngredient>> blocks_north;
    private final Map<BlockPos, List<BlockIngredient>> blocks_east;
    private final Map<BlockPos, List<BlockIngredient>> blocks_south;
    private final Map<BlockPos, List<BlockIngredient>> blocks_west;

    public BlockStructure(Map<BlockPos, List<BlockIngredient>> blocks) {
        this.blocks_south = blocks;
        this.blocks_west = rotate(blocks, Rotation.CLOCKWISE_90);
        this.blocks_north = rotate(blocks, Rotation.CLOCKWISE_180);
        this.blocks_east = rotate(blocks, Rotation.COUNTERCLOCKWISE_90);
    }

    public Map<BlockPos, List<BlockIngredient>> getBlocks(Direction direction) {
        return switch (direction) {
            case SOUTH -> blocks_south;
            case EAST -> this.blocks_east;
            case WEST -> this.blocks_west;
            default -> this.blocks_north;
        };
    }

    public boolean match(LevelReader world, BlockPos machinePos, Direction machineFacing) {
        Map<BlockPos, List<BlockIngredient>> blocks = getBlocks(machineFacing);
        BlockPos.MutableBlockPos worldPos = new BlockPos.MutableBlockPos();
        for(BlockPos pos : blocks.keySet()) {
            List<BlockIngredient> ingredients = blocks.get(pos);
            worldPos.set(pos.getX() + machinePos.getX(), pos.getY() + machinePos.getY(), pos.getZ() + machinePos.getZ());
            BlockInWorld info = new BlockInWorld(world, worldPos, false);
            if(ingredients.stream().noneMatch(blockIngredient -> blockIngredient.test(info)))
                return false;
        }
        return true;
    }

    private Map<BlockPos, List<BlockIngredient>> rotate(Map<BlockPos, List<BlockIngredient>> blocks, Rotation rotation) {
        Map<BlockPos, List<BlockIngredient>> rotated = new HashMap<>();
        blocks.forEach((pos, ingredients) -> {
            List<BlockIngredient> rotatedIngredients = ingredients.stream().map(blockIngredient -> blockIngredient.rotate(rotation)).toList();
            rotated.put(pos.rotate(rotation), rotatedIngredients);
        });
        return rotated;
    }

    public static class Builder {

        private static final Joiner COMMA_JOIN = Joiner.on(",");
        private final List<String[]> depth = Lists.newArrayList();
        private final Map<Character, List<BlockIngredient>> symbolMap = Maps.newHashMap();
        private int aisleHeight;
        private int rowWidth;

        private Builder() {
            this.symbolMap.put(' ', List.of(BlockIngredient.ANY));
            this.symbolMap.put('m', List.of(BlockIngredient.MACHINE));
        }

        /**
         * Adds a single aisle to this pattern, going in the y-axis. (so multiple calls to this will increase the y-size by 1)
         */
        public void aisle(String... aisle) {
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
                }
            } else {
                throw new IllegalArgumentException("Empty pattern for aisle");
            }
        }

        public static Builder start() {
            return new Builder();
        }

        public void where(char symbol, List<BlockIngredient> blockMatcher) {
            this.symbolMap.put(symbol, blockMatcher);
        }

        public BlockStructure build() {
            this.checkMissingPredicates();
            BlockPos machinePos = this.getMachinePos();
            Map<BlockPos, List<BlockIngredient>> blocks = new HashMap<>();
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

            for(Map.Entry<Character, List<BlockIngredient>> entry : this.symbolMap.entrySet()) {
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

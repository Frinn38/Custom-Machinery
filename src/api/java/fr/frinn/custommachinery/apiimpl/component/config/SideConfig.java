package fr.frinn.custommachinery.apiimpl.component.config;

import com.google.common.collect.Maps;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class SideConfig {

    public static final Map<RelativeSide, SideMode> DEFAULT_ALL_BOTH = Util.make(() -> {
        EnumMap<RelativeSide, SideMode> map = Maps.newEnumMap(RelativeSide.class);
        for(RelativeSide side : RelativeSide.values())
            map.put(side, SideMode.BOTH);
        return map;
    });

    private final Map<RelativeSide, SideMode> sides = new HashMap<>();
    private final IMachineComponentManager manager;

    public SideConfig(IMachineComponentManager manager, Map<RelativeSide, SideMode> defaultConfig) {
        this.manager = manager;
        this.sides.putAll(defaultConfig);
    }

    private Direction facing() {
        return this.manager.getTile().getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public SideMode getSideMode(RelativeSide side) {
        return this.sides.get(side);
    }

    public SideMode getSideMode(Direction direction) {
        return getSideMode(RelativeSide.fromDirections(facing(), direction));
    }

    public void setSideMode(RelativeSide side, SideMode mode) {
        this.sides.put(side, mode);
    }

    public Tag serialize() {
        CompoundTag nbt = new CompoundTag();
        this.sides.forEach((side, mode) -> nbt.put(side.name(), ByteTag.valueOf((byte)mode.ordinal())));
        return nbt;
    }

    public void deserialize(Tag tag) {
        if(tag instanceof CompoundTag nbt) {
            for(RelativeSide side : RelativeSide.values())
                if(nbt.get(side.name()) instanceof ByteTag byteTag)
                    this.sides.put(side, SideMode.values()[byteTag.getAsInt()]);
        }
    }
}

package fr.frinn.custommachinery.impl.component.config;

import fr.frinn.custommachinery.api.component.ISideConfigComponent;
import fr.frinn.custommachinery.impl.component.config.SideConfig.SideMode;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.HashMap;
import java.util.Map;

public abstract class SideConfig<M extends SideMode> {

    final Map<RelativeSide, M> sides = new HashMap<>();
    private final ISideConfigComponent component;

    private final boolean enabled;
    private TriConsumer<RelativeSide, M, M> callback;

    public SideConfig(ISideConfigComponent component, Map<RelativeSide, M> defaultConfig, boolean enabled) {
        this.component = component;
        this.sides.putAll(defaultConfig);
        this.enabled = enabled;
    }

    private Direction facing() {
        return this.component.getManager().getTile().getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public ISideConfigComponent getComponent() {
        return this.component;
    }

    public M getSideMode(RelativeSide side) {
        return this.sides.get(side);
    }

    public M getSideMode(Direction direction) {
        return getSideMode(RelativeSide.fromDirections(facing(), direction));
    }

    public void setSideMode(RelativeSide side, M mode) {
        M oldMode = this.sides.put(side, mode);
        if(this.callback != null && !getComponent().getManager().getLevel().isClientSide())
            this.callback.accept(side, oldMode, mode);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void set(SideConfig<M> config) {
        for(RelativeSide side : RelativeSide.values())
            setSideMode(side, config.getSideMode(side));
    }

    public void setCallback(TriConsumer<RelativeSide, M, M> callback) {
        this.callback = callback;
    }

    public abstract void setNext(RelativeSide side);

    public abstract void setPrevious(RelativeSide side);

    public abstract SideConfig<M> copy();

    public abstract CompoundTag serialize();

    public abstract void deserialize(CompoundTag nbt);

    public interface SideMode {
        Component title();
        int color();
    }

    public interface Template<M extends SideMode> {
        Map<RelativeSide, M> sides();
        boolean enabled();
    }
}

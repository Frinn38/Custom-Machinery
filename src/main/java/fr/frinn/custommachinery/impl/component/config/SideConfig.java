package fr.frinn.custommachinery.impl.component.config;

import fr.frinn.custommachinery.api.component.ISideConfigComponent;
import fr.frinn.custommachinery.common.util.Color;
import fr.frinn.custommachinery.impl.component.config.SideConfig.SideMode;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.EnumMap;
import java.util.Map;

public abstract class SideConfig<M extends SideMode> {

    public static final Color DEFAULT_COLOR = Color.fromColors(0.5, 0, 0, 1);

    final Map<RelativeSide, M> sides = new EnumMap<>(RelativeSide.class);
    private final ISideConfigComponent component;
    private final Direction facing;
    private final boolean enabled;
    //Color of the slot in the MachineConfigScreen
    private final Color color;
    private TriConsumer<RelativeSide, M, M> callback;

    public SideConfig(ISideConfigComponent component, Map<RelativeSide, M> defaultConfig, boolean enabled, Color color) {
        this.component = component;
        this.facing = component != null ? component.getManager().facing() : Direction.NORTH;
        this.sides.putAll(defaultConfig);
        this.enabled = enabled;
        this.color = color;
    }

    public ISideConfigComponent getComponent() {
        return this.component;
    }

    public M getSideMode(RelativeSide side) {
        return this.sides.get(side);
    }

    public M getSideMode(Direction direction) {
        return getSideMode(RelativeSide.fromDirections(this.facing, direction));
    }

    public void setSideMode(RelativeSide side, M mode) {
        M oldMode = this.sides.put(side, mode);
        if(this.callback != null && !getComponent().getManager().getLevel().isClientSide())
            this.callback.accept(side, oldMode, mode);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public Color getColor() {
        return this.color;
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
        Color color();
    }
}

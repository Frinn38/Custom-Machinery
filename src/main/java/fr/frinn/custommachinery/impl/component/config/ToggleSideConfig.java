package fr.frinn.custommachinery.impl.component.config;

import com.google.common.collect.Maps;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.common.util.Color;
import fr.frinn.custommachinery.impl.codec.EnumMapCodec;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;

import java.util.EnumMap;
import java.util.Map;

public class ToggleSideConfig extends SideConfig<ToggleSideMode> {

    public ToggleSideConfig(Direction facing, Map<RelativeSide, ToggleSideMode> defaultConfig, boolean enabled, Color color, ConfigGuiData guiData) {
        super(facing, defaultConfig, enabled, color, guiData);
    }

    @Override
    public void setNext(RelativeSide side) {
        this.setSideMode(side, this.getSideMode(side) == ToggleSideMode.ENABLED ? ToggleSideMode.DISABLED : ToggleSideMode.ENABLED);
    }

    @Override
    public void setPrevious(RelativeSide side) {
        this.setSideMode(side, this.getSideMode(side) == ToggleSideMode.ENABLED ? ToggleSideMode.DISABLED : ToggleSideMode.ENABLED);
    }

    @Override
    public ToggleSideConfig copy() {
        return new ToggleSideConfig(this.getFacing(), this.sides, this.isEnabled(), this.getColor(), this.getGuiData());
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag nbt = new CompoundTag();
        this.sides.forEach((side, mode) -> nbt.put(side.name(), ByteTag.valueOf(mode.isEnabled())));
        return nbt;
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        for(RelativeSide side : RelativeSide.values())
            if(nbt.get(side.name()) instanceof ByteTag byteTag)
                this.sides.put(side, byteTag == ByteTag.ONE ? ToggleSideMode.ENABLED : ToggleSideMode.DISABLED);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        else if(obj instanceof ToggleSideConfig config) {
            for(RelativeSide side : RelativeSide.values()) {
                if(config.getSideMode(side) != this.getSideMode(side))
                    return false;
            }
            return true;
        }
        return false;
    }

    public record Template(Map<RelativeSide, ToggleSideMode> sides, boolean enabled, Color color, ConfigGuiData guiData) implements SideConfig.Template<ToggleSideMode> {

        public static final NamedCodec<Template> CODEC = NamedCodec.record(templateInstance ->
                templateInstance.group(
                        EnumMapCodec.of(RelativeSide.class, ToggleSideMode.CODEC, ToggleSideMode.ENABLED).forGetter(template -> template.sides),
                        NamedCodec.BOOL.optionalFieldOf("enabled", true).forGetter(Template::enabled),
                        Color.CODEC.optionalFieldOf("color", DEFAULT_COLOR).forGetter(Template::color),
                        ConfigGuiData.CODEC.optionalFieldOf("gui", ConfigGuiData.DEFAULT).forGetter(Template::guiData)
                ).apply(templateInstance, Template::new), "Toggle Side Config Template");

        public static final Template DEFAULT_ALL_ENABLED = makeDefault(ToggleSideMode.ENABLED, true);
        public static final Template DEFAULT_ALL_DISABLED = makeDefault(ToggleSideMode.DISABLED, true);
        public static final Template DEFAULT_ALL_DISABLED_DISABLED = makeDefault(ToggleSideMode.DISABLED, false);

        private static Template makeDefault(ToggleSideMode defaultMode, boolean enabled) {
            EnumMap<RelativeSide, ToggleSideMode> map = Maps.newEnumMap(RelativeSide.class);
            for(RelativeSide side : RelativeSide.values())
                map.put(side, defaultMode);
            return new Template(map, enabled, DEFAULT_COLOR, ConfigGuiData.DEFAULT);
        }

        public ToggleSideConfig build(Direction facing) {
            return new ToggleSideConfig(facing, this.sides, this.enabled, this.color, this.guiData);
        }
    }
}

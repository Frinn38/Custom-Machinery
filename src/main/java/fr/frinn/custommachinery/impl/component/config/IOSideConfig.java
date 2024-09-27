package fr.frinn.custommachinery.impl.component.config;

import com.google.common.collect.Maps;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.ISideConfigComponent;
import fr.frinn.custommachinery.impl.codec.EnumMapCodec;
import fr.frinn.custommachinery.impl.component.config.SideConfig.Template;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;

import java.util.EnumMap;
import java.util.Map;

public class IOSideConfig extends SideConfig<IOSideMode> {

    private boolean autoInput;
    private boolean autoOutput;

    public IOSideConfig(ISideConfigComponent component, Map<RelativeSide, IOSideMode> defaultConfig, boolean input, boolean output, boolean enabled) {
        super(component, defaultConfig, enabled);
        this.autoInput = input;
        this.autoOutput = output;
    }

    public boolean isAutoInput() {
        return this.autoInput;
    }

    public boolean isAutoOutput() {
        return this.autoOutput;
    }

    public void setAutoInput(boolean autoInput) {
        this.autoInput = autoInput;
    }

    public void setAutoOutput(boolean autoOutput) {
        this.autoOutput = autoOutput;
    }

    @Override
    public void set(SideConfig<IOSideMode> config) {
        super.set(config);
        if(config instanceof IOSideConfig ioSideConfig) {
            setAutoInput(ioSideConfig.isAutoInput());
            setAutoOutput(ioSideConfig.isAutoOutput());
        }
    }

    @Override
    public void setNext(RelativeSide side) {
        this.setSideMode(side, this.getSideMode(side).next());
    }

    @Override
    public void setPrevious(RelativeSide side) {
        this.setSideMode(side, this.getSideMode(side).previous());
    }

    @Override
    public IOSideConfig copy() {
        return new IOSideConfig(this.getComponent(), this.sides, this.autoInput, this.autoOutput, this.isEnabled());
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag nbt = new CompoundTag();
        this.sides.forEach((side, mode) -> nbt.put(side.name(), ByteTag.valueOf((byte)mode.ordinal())));
        nbt.putBoolean("input", this.autoInput);
        nbt.putBoolean("output", this.autoOutput);
        return nbt;
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        for(RelativeSide side : RelativeSide.values())
            if(nbt.get(side.name()) instanceof ByteTag byteTag)
                this.sides.put(side, IOSideMode.values()[byteTag.getAsInt()]);
        this.autoInput = nbt.getBoolean("input");
        this.autoOutput = nbt.getBoolean("output");
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        else if(obj instanceof IOSideConfig config) {
            for(RelativeSide side : RelativeSide.values()) {
                if(config.getSideMode(side) != this.getSideMode(side))
                    return false;
            }
            return config.isAutoInput() == this.isAutoInput() && config.isAutoOutput() == this.isAutoOutput();
        }
        return false;
    }

    public record Template(Map<RelativeSide, IOSideMode> sides, boolean autoInput, boolean autoOutput, boolean enabled) implements SideConfig.Template<IOSideMode> {

        public static final NamedCodec<Template> CODEC = NamedCodec.record(templateInstance ->
                templateInstance.group(
                        EnumMapCodec.of(RelativeSide.class, IOSideMode.CODEC, IOSideMode.BOTH).forGetter(Template::sides),
                        NamedCodec.BOOL.optionalFieldOf("input", false).forGetter(Template::autoInput),
                        NamedCodec.BOOL.optionalFieldOf("output", false).forGetter(Template::autoOutput),
                        NamedCodec.BOOL.optionalFieldOf("enabled", true).forGetter(Template::enabled)
                ).apply(templateInstance, Template::new), "IO Side Config Template");

        public static final Template DEFAULT_ALL_BOTH = makeDefault(IOSideMode.BOTH, true);
        public static final Template DEFAULT_ALL_INPUT = makeDefault(IOSideMode.INPUT, true);
        public static final Template DEFAULT_ALL_OUTPUT = makeDefault(IOSideMode.OUTPUT, true);
        public static final Template DEFAULT_ALL_NONE = makeDefault(IOSideMode.NONE, true);
        public static final Template DEFAULT_ALL_NONE_DISABLED = makeDefault(IOSideMode.NONE, false);

        private static Template makeDefault(IOSideMode defaultMode, boolean enabled) {
            EnumMap<RelativeSide, IOSideMode> map = Maps.newEnumMap(RelativeSide.class);
            for(RelativeSide side : RelativeSide.values())
                map.put(side, defaultMode);
            return new Template(map, false, false, enabled);
        }

        public <T extends ISideConfigComponent> IOSideConfig build(T component) {
            return new IOSideConfig(component, this.sides, this.autoInput, this.autoOutput, this.enabled);
        }
    }
}

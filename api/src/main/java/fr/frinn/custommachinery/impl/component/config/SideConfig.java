package fr.frinn.custommachinery.impl.component.config;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.component.ISideConfigComponent;
import fr.frinn.custommachinery.impl.codec.CodecLogger;
import fr.frinn.custommachinery.impl.codec.EnumMapCodec;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class SideConfig {

    private final Map<RelativeSide, SideMode> sides = new HashMap<>();
    private final ISideConfigComponent component;

    private boolean autoInput;
    private boolean autoOutput;
    private final boolean enabled;
    private TriConsumer<RelativeSide, SideMode, SideMode> callback;

    public SideConfig(ISideConfigComponent component, Map<RelativeSide, SideMode> defaultConfig, boolean autoInput, boolean autoOutput, boolean enabled) {
        this.component = component;
        this.sides.putAll(defaultConfig);
        this.autoInput = autoInput;
        this.autoOutput = autoOutput;
        this.enabled = enabled;
    }

    private Direction facing() {
        return this.component.getManager().getTile().getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public ISideConfigComponent getComponent() {
        return this.component;
    }

    public SideMode getSideMode(RelativeSide side) {
        return this.sides.get(side);
    }

    public SideMode getSideMode(Direction direction) {
        return getSideMode(RelativeSide.fromDirections(facing(), direction));
    }

    public void setSideMode(RelativeSide side, SideMode mode) {
        SideMode oldMode = this.sides.put(side, mode);
        if(this.callback != null && !getComponent().getManager().getLevel().isClientSide())
            this.callback.accept(side, oldMode, mode);
    }

    public boolean isAutoInput() {
        return this.autoInput;
    }

    public boolean isAutoOutput() {
        return this.autoOutput;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setAutoInput(boolean autoInput) {
        this.autoInput = autoInput;
    }

    public void setAutoOutput(boolean autoOutput) {
        this.autoOutput = autoOutput;
    }

    public void set(SideConfig config) {
        for(RelativeSide side : RelativeSide.values())
            setSideMode(side, config.getSideMode(side));
        setAutoInput(config.isAutoInput());
        setAutoOutput(config.isAutoOutput());
    }

    public void setCallback(TriConsumer<RelativeSide, SideMode, SideMode> callback) {
        this.callback = callback;
    }

    public SideConfig copy() {
        return new SideConfig(this.component, this.sides, this.autoInput, this.autoOutput, this.enabled);
    }

    public Tag serialize() {
        CompoundTag nbt = new CompoundTag();
        this.sides.forEach((side, mode) -> nbt.put(side.name(), ByteTag.valueOf((byte)mode.ordinal())));
        nbt.putBoolean("input", this.autoInput);
        nbt.putBoolean("output", this.autoOutput);
        return nbt;
    }

    public void deserialize(Tag tag) {
        if(tag instanceof CompoundTag nbt) {
            for(RelativeSide side : RelativeSide.values())
                if(nbt.get(side.name()) instanceof ByteTag byteTag)
                    this.sides.put(side, SideMode.values()[byteTag.getAsInt()]);
            this.autoInput = nbt.getBoolean("input");
            this.autoOutput = nbt.getBoolean("output");
        }
    }

    public static class Template {

        public static final Codec<Template> CODEC = RecordCodecBuilder.create(templateInstance ->
            templateInstance.group(
                    EnumMapCodec.of(RelativeSide.class, RelativeSide.CODEC, SideMode.CODEC, SideMode.BOTH).forGetter(template -> template.sides),
                    CodecLogger.loggedOptional(Codec.BOOL, "input", false).forGetter(template -> template.autoInput),
                    CodecLogger.loggedOptional(Codec.BOOL, "output", false).forGetter(template -> template.autoOutput),
                    CodecLogger.loggedOptional(Codec.BOOL, "enabled", true).forGetter(template -> template.enabled)
            ).apply(templateInstance, Template::new)
        );

        public static final Template DEFAULT_ALL_BOTH = makeDefault(SideMode.BOTH);
        public static final Template DEFAULT_ALL_INPUT = makeDefault(SideMode.INPUT);
        public static final Template DEFAULT_ALL_OUTPUT = makeDefault(SideMode.OUTPUT);
        public static final Template DEFAULT_ALL_NONE = makeDefault(SideMode.NONE);
        private static Template makeDefault(SideMode defaultMode) {
            EnumMap<RelativeSide, SideMode> map = Maps.newEnumMap(RelativeSide.class);
            for(RelativeSide side : RelativeSide.values())
                map.put(side, defaultMode);
            return new Template(map, false, false, true);
        }

        private final Map<RelativeSide, SideMode> sides;
        private final boolean autoInput;
        private final boolean autoOutput;
        private final boolean enabled;

        private Template(Map<RelativeSide, SideMode> sides, boolean autoInput, boolean autoOutput, boolean enabled) {
            this.sides = sides;
            this.autoInput = autoInput;
            this.autoOutput = autoOutput;
            this.enabled = enabled;
        }

        public <T extends ISideConfigComponent> SideConfig build(T component) {
            return new SideConfig(component, this.sides, this.autoInput, this.autoOutput, this.enabled);
        }
    }
}

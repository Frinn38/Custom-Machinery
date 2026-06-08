package fr.frinn.custommachinery.impl.component.config;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.ISideConfigComponent;
import fr.frinn.custommachinery.common.util.Color;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.component.config.SideConfig.SideMode;
import fr.frinn.custommachinery.impl.util.TextComponentUtils;
import fr.frinn.custommachinery.impl.util.TextureInfo;
import fr.frinn.custommachinery.impl.util.TextureSizeHelper;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public abstract class SideConfig<M extends SideMode> {

    public static final Color DEFAULT_COLOR = Color.fromColors(0.5, 0, 0, 1);

    final Map<RelativeSide, M> sides = new EnumMap<>(RelativeSide.class);
    private final ISideConfigComponent component;
    private final Direction facing;
    private final boolean enabled;
    //Color of the slot in the MachineConfigScreen
    private final Color color;
    private final ConfigGuiData guiData;
    private TriConsumer<RelativeSide, M, M> callback;

    public SideConfig(ISideConfigComponent component, Map<RelativeSide, M> defaultConfig, boolean enabled, Color color, ConfigGuiData guiData) {
        this.component = component;
        this.facing = component != null ? component.getManager().facing() : Direction.NORTH;
        this.sides.putAll(defaultConfig);
        this.enabled = enabled;
        this.color = color;
        this.guiData = guiData;
    }

    public ISideConfigComponent getComponent() {
        return this.component;
    }

    public M getSideMode(RelativeSide side) {
        return this.sides.get(side);
    }

    public M getDirectionMode(Direction direction) {
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

    public ConfigGuiData getGuiData() {
        return this.guiData;
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
        ConfigGuiData guiData();
    }

    public record ConfigGuiData(int width,
                                int height,
                                TextureInfo background,
                                Component title,
                                ConfigButtonData exit,
                                ConfigButtonData top,
                                ConfigButtonData bottom,
                                ConfigButtonData left,
                                ConfigButtonData right,
                                ConfigButtonData front,
                                ConfigButtonData back,
                                ConfigButtonData none,
                                ConfigButtonData input,
                                ConfigButtonData output) {

        public static final TextureInfo DEFAULT_BACKGROUND = new TextureInfo(CustomMachinery.rl("textures/gui/sprites/config/background.png"));
        public static final Component DEFAULT_TITLE = Component.translatable("custommachinery.gui.config.component");

        public static final WidgetSprites DEFAULT_EXIT_SPRITES = new WidgetSprites(CustomMachinery.rl("config/exit_button"), CustomMachinery.rl("config/exit_button_hovered"));
        public static final ConfigButtonData DEFAULT_EXIT = new ConfigButtonData(5, 5, 9, 9, DEFAULT_EXIT_SPRITES);

        public static final WidgetSprites DEFAULT_SIDE_MODE_SPRITES = new WidgetSprites(CustomMachinery.rl("config/side_mode_button"), CustomMachinery.rl("config/side_mode_button_hovered"));
        public static final ConfigButtonData DEFAULT_TOP = new ConfigButtonData(41, 25, 14, 14, DEFAULT_SIDE_MODE_SPRITES);
        public static final ConfigButtonData DEFAULT_BOTTOM = new ConfigButtonData(41, 57, 14, 14, DEFAULT_SIDE_MODE_SPRITES);
        public static final ConfigButtonData DEFAULT_LEFT = new ConfigButtonData(25, 41, 14, 14, DEFAULT_SIDE_MODE_SPRITES);
        public static final ConfigButtonData DEFAULT_RIGHT = new ConfigButtonData(57, 41, 14, 14, DEFAULT_SIDE_MODE_SPRITES);
        public static final ConfigButtonData DEFAULT_FRONT = new ConfigButtonData(41, 41, 14, 14, DEFAULT_SIDE_MODE_SPRITES);
        public static final ConfigButtonData DEFAULT_BACK = new ConfigButtonData(25, 57, 14, 14, DEFAULT_SIDE_MODE_SPRITES);

        public static final WidgetSprites DEFAULT_NONE_SPRITES = new WidgetSprites(CustomMachinery.rl("config/all_none_button"), CustomMachinery.rl("config/all_none_button_hovered"));
        public static final ConfigButtonData DEFAULT_NONE = new ConfigButtonData(78, 57, 14, 14, DEFAULT_NONE_SPRITES);

        public static final WidgetSprites DEFAULT_IO_SPRITES = new WidgetSprites(CustomMachinery.rl("config/auto_io_button"), CustomMachinery.rl("config/auto_io_button_hovered"));
        public static final ConfigButtonData DEFAULT_INPUT = new ConfigButtonData(18, 75, 28, 14, DEFAULT_IO_SPRITES);
        public static final ConfigButtonData DEFAULT_OUTPUT = new ConfigButtonData(50, 75, 28, 14, DEFAULT_IO_SPRITES);

        public static final ConfigGuiData DEFAULT = new ConfigGuiData(96, 96, DEFAULT_BACKGROUND, DEFAULT_TITLE, DEFAULT_EXIT, DEFAULT_TOP, DEFAULT_BOTTOM, DEFAULT_LEFT, DEFAULT_RIGHT, DEFAULT_FRONT, DEFAULT_BACK, DEFAULT_NONE, DEFAULT_INPUT, DEFAULT_OUTPUT);

        public static final NamedCodec<ConfigGuiData> CODEC = NamedCodec.record(configGuiDataInstance ->
                configGuiDataInstance.group(
                        NamedCodec.intRange(-1, Integer.MAX_VALUE).optionalFieldOf("width", -1).forGetter(ConfigGuiData::width),
                        NamedCodec.intRange(-1, Integer.MAX_VALUE).optionalFieldOf("height", -1).forGetter(ConfigGuiData::height),
                        TextureInfo.CODEC.optionalFieldOf("background", DEFAULT_BACKGROUND).forGetter(ConfigGuiData::background),
                        TextComponentUtils.CODEC.optionalFieldOf("title", DEFAULT_TITLE).forGetter(ConfigGuiData::title),
                        ConfigButtonData.CODEC.optionalFieldOf("exit", DEFAULT_EXIT).forGetter(ConfigGuiData::exit),
                        ConfigButtonData.CODEC.optionalFieldOf("top", DEFAULT_TOP).forGetter(ConfigGuiData::top),
                        ConfigButtonData.CODEC.optionalFieldOf("bottom", DEFAULT_BOTTOM).forGetter(ConfigGuiData::bottom),
                        ConfigButtonData.CODEC.optionalFieldOf("left", DEFAULT_LEFT).forGetter(ConfigGuiData::left),
                        ConfigButtonData.CODEC.optionalFieldOf("right", DEFAULT_RIGHT).forGetter(ConfigGuiData::right),
                        ConfigButtonData.CODEC.optionalFieldOf("front", DEFAULT_FRONT).forGetter(ConfigGuiData::front),
                        ConfigButtonData.CODEC.optionalFieldOf("back", DEFAULT_BACK).forGetter(ConfigGuiData::back),
                        ConfigButtonData.CODEC.optionalFieldOf("none", DEFAULT_NONE).forGetter(ConfigGuiData::none),
                        ConfigButtonData.CODEC.optionalFieldOf("input", DEFAULT_INPUT).forGetter(ConfigGuiData::input),
                        ConfigButtonData.CODEC.optionalFieldOf("output", DEFAULT_OUTPUT).forGetter(ConfigGuiData::output)
                ).apply(configGuiDataInstance, ConfigGuiData::new), "Config gui data"
        );

        @Override
        public int width() {
            if(this.width >= 0)
                return this.width;
            else if(FMLLoader.getDist() == Dist.CLIENT)
                return TextureSizeHelper.getTextureWidth(this.background.texture());
            else
                return -1;
        }

        @Override
        public int height() {
            if(this.height >= 0)
                return this.height;
            else if(FMLLoader.getDist() == Dist.CLIENT)
                return TextureSizeHelper.getTextureHeight(this.background.texture());
            else
                return -1;
        }
    }

    public record ConfigButtonData(int x, int y, int width, int height, WidgetSprites sprites) {
        public static final NamedCodec<WidgetSprites> SPRITE_CODEC = NamedCodec.record(widgetSpritesInstance ->
                widgetSpritesInstance.group(
                        DefaultCodecs.RESOURCE_LOCATION.fieldOf("texture").forGetter(WidgetSprites::enabled),
                        DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("texture_hovered").forGetter(sprites -> Optional.of(sprites.enabled()))
                ).apply(widgetSpritesInstance, (texture, texture_hovered) -> new WidgetSprites(texture, texture_hovered.orElse(texture))), "Widget sprites"
        );

        public static final NamedCodec<ConfigButtonData> CODEC = NamedCodec.record(configButtonDataInstance ->
                configButtonDataInstance.group(
                        NamedCodec.intRange(0, Integer.MAX_VALUE).fieldOf("x").forGetter(ConfigButtonData::x),
                        NamedCodec.intRange(0, Integer.MAX_VALUE).fieldOf("y").forGetter(ConfigButtonData::y),
                        NamedCodec.intRange(-1, Integer.MAX_VALUE).optionalFieldOf("width", -1).forGetter(ConfigButtonData::width),
                        NamedCodec.intRange(-1, Integer.MAX_VALUE).optionalFieldOf("height", -1).forGetter(ConfigButtonData::height),
                        SPRITE_CODEC.fieldOf("textures").forGetter(ConfigButtonData::sprites)
                ).apply(configButtonDataInstance, ConfigButtonData::new), "Config button data"
        );
    }
}

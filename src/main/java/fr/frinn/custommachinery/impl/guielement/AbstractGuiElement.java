package fr.frinn.custommachinery.impl.guielement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.codec.NamedMapCodec;
import fr.frinn.custommachinery.impl.util.TextComponentUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class AbstractGuiElement implements IGuiElement {

    public record Properties(int x, int y, int width, int height, int priority, @Nullable ResourceLocation texture, @Nullable ResourceLocation textureHovered, List<Component> tooltips, String id){}

    private final Properties properties;

    public AbstractGuiElement(Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        return this.properties;
    }

    @Override
    public int getX() {
        return this.properties.x();
    }

    @Override
    public int getY() {
        return this.properties.y();
    }

    @Override
    public int getWidth() {
        return this.properties.width();
    }

    @Override
    public int getHeight() {
        return this.properties.height();
    }

    @Override
    public int getPriority() {
        return this.properties.priority();
    }

    @Override
    public List<Component> getTooltips() {
        return this.properties.tooltips();
    }

    @Override
    public String getId() {
        return this.properties.id();
    }

    public static NamedMapCodec<Properties> makePropertiesCodec() {
        return makePropertiesCodec(null, null, Collections.emptyList());
    }

    public static NamedMapCodec<Properties> makePropertiesCodec(@Nullable ResourceLocation defaultTexture) {
        return makePropertiesCodec(defaultTexture, null, Collections.emptyList());
    }

    public static NamedMapCodec<Properties> makePropertiesCodec(@Nullable ResourceLocation defaultTexture, @Nullable ResourceLocation defaultTextureHovered) {
        return makePropertiesCodec(defaultTexture, defaultTextureHovered, Collections.emptyList());
    }

    public static NamedMapCodec<Properties> makePropertiesCodec(@Nullable ResourceLocation defaultTexture, @Nullable ResourceLocation defaultTextureHovered, @NotNull List<Component> defaultTooltips) {
        return NamedCodec.record(propertiesInstance ->
             propertiesInstance.group(
                     NamedCodec.intRange(0, Integer.MAX_VALUE).fieldOf("x").forGetter(Properties::x),
                     NamedCodec.intRange(0, Integer.MAX_VALUE).fieldOf("y").forGetter(Properties::y),
                     NamedCodec.intRange(-1, Integer.MAX_VALUE).optionalFieldOf("width", -1).forGetter(Properties::width),
                     NamedCodec.intRange(-1, Integer.MAX_VALUE).optionalFieldOf("height", -1).forGetter(Properties::height),
                     NamedCodec.INT.optionalFieldOf("priority", 0).forGetter(Properties::priority),
                     DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("texture").forGetter(properties -> defaultTexture != null && defaultTexture.equals(properties.texture()) ? Optional.empty() : Optional.ofNullable(properties.texture())),
                     DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("texture_hovered").forGetter(properties -> defaultTextureHovered != null && defaultTextureHovered.equals(properties.textureHovered()) ? Optional.empty() : Optional.ofNullable(properties.textureHovered())),
                     TextComponentUtils.CODEC.listOf().optionalFieldOf("tooltips").forGetter(properties -> (!defaultTooltips.isEmpty() && defaultTooltips.equals(properties.tooltips())) || (defaultTooltips.isEmpty() && properties.tooltips().isEmpty()) ? Optional.empty() : Optional.of(properties.tooltips())),
                     NamedCodec.STRING.optionalFieldOf("id", "").forGetter(Properties::id)
             ).apply(propertiesInstance, (x, y, width, height, priority, texture, textureHovered, tooltips, id) ->
                     new Properties(x, y, width, height, priority, texture.orElse(defaultTexture), textureHovered.orElse(defaultTextureHovered), tooltips.orElse(defaultTooltips), id)
             ), "Gui element properties");
    }
}

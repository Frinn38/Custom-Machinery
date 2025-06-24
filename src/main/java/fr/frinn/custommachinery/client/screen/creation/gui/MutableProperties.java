package fr.frinn.custommachinery.client.screen.creation.gui;

import fr.frinn.custommachinery.impl.guielement.AbstractGuiElement.Properties;
import fr.frinn.custommachinery.impl.util.TextureInfo;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class MutableProperties {

    private int x = 0;
    private int y = 0;
    private int width = -1;
    private int height = -1;
    private int priority = 0;
    @Nullable
    private TextureInfo texture = null;
    @Nullable
    private TextureInfo textureHovered = null;
    private List<Component> tooltips = Collections.emptyList();
    private String id = "";

    public MutableProperties(Properties properties) {
        this.x = properties.x();
        this.y = properties.y();
        this.width = properties.width();
        this.height = properties.height();
        this.priority = properties.priority();
        this.texture = properties.texture();
        this.textureHovered = properties.textureHovered();
        this.tooltips = properties.tooltips();
        this.id = properties.id();
    }

    public MutableProperties() {

    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Nullable
    public TextureInfo getTexture() {
        return this.texture;
    }

    public void setTexture(@Nullable TextureInfo texture) {
        this.texture = texture;
    }

    @Nullable
    public TextureInfo getTextureHovered() {
        return this.textureHovered;
    }

    public void setTextureHovered(@Nullable TextureInfo textureHovered) {
        this.textureHovered = textureHovered;
    }

    public List<Component> getTooltips() {
        return this.tooltips;
    }

    public void setTooltips(List<Component> tooltips) {
        this.tooltips = tooltips;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Properties build() {
        return new Properties(this.x, this.y, this.width, this.height, this.priority, this.texture, this.textureHovered, this.tooltips, this.id);
    }
}

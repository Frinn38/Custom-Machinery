package fr.frinn.custommachinery.client.screen.widget;

import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.impl.util.TextureInfo;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class TexturedButton extends Button {

    public static Builder builder(Component message, TextureInfo texture, OnPress onPress) {
        return new Builder(message, texture, onPress);
    }

    private final TextureInfo texture;
    @Nullable
    private final TextureInfo textureHovered;

    private TexturedButton(int x, int y, int width, int height, TextureInfo texture, @Nullable TextureInfo textureHovered, OnPress onPress, Component message, CreateNarration narration) {
        super(x, y, width, height, message, onPress, narration);
        this.texture = texture;
        this.textureHovered = textureHovered;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if(this.isHovered() && this.textureHovered != null)
            ClientHandler.blit(graphics, this.textureHovered, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        else
            ClientHandler.blit(graphics, this.texture, this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }

    public static class Builder {
        private final Component message;
        private final OnPress onPress;
        private final TextureInfo texture;
        @Nullable
        private Tooltip tooltip;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;
        @Nullable
        private TextureInfo textureHovered;

        private Builder(Component message, TextureInfo texture, OnPress onPress) {
            this.message = message;
            this.texture = texture;
            this.onPress = onPress;
        }

        public Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder bounds(int x, int y, int width, int height) {
            return this.pos(x, y).size(width, height);
        }

        public Builder hovered(@Nullable TextureInfo textureHovered) {
            this.textureHovered = textureHovered;
            return this;
        }

        public Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public TexturedButton build() {
            TexturedButton button = new TexturedButton(this.x, this.y, this.width, this.height, this.texture, this.textureHovered, this.onPress, this.message, DEFAULT_NARRATION);
            button.setTooltip(this.tooltip);
            return button;
        }
    }
}

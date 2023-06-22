package fr.frinn.custommachinery.client.screen.widget.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.impl.util.TextureSizeHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ButtonWidget extends Widget {

    private Component title = Component.empty();
    private boolean displayTitle = false;
    private Consumer<ButtonWidget> onPress = button -> {};
    private ResourceLocation texture = null;
    private int u = 0;
    private int v = 0;
    private int uWidth;
    private int vHeight;
    private ResourceLocation hoverTexture = null;
    private int hoverU = 0;
    private int hoverV = 0;
    private int hoverUWidth;
    private int hoverVHeight;
    private boolean background = true;
    private final List<Component> tooltips = new ArrayList<>();

    public ButtonWidget(Supplier<Integer> x, Supplier<Integer> y, int width, int height) {
        super(x, y, width, height);
        this.uWidth = width;
        this.vHeight = height;
        this.hoverUWidth = width;
        this.hoverVHeight = height;
    }

    public ButtonWidget title(Component title, boolean display) {
        this.title = title;
        this.displayTitle = display;
        return this;
    }

    public ButtonWidget callback(Consumer<ButtonWidget> onPress) {
        this.onPress = onPress;
        return this;
    }

    public ButtonWidget texture(ResourceLocation texture) {
        this.texture = texture;
        return this;
    }

    public ButtonWidget texture(ResourceLocation texture, int u, int v) {
        this.u = u;
        this.v = v;
        return texture(texture);
    }

    public ButtonWidget texture(ResourceLocation texture, int u, int v, int uWidth, int vHeight) {
        this.uWidth = uWidth;
        this.vHeight = vHeight;
        return texture(texture, u, v);
    }

    public ButtonWidget hoverTexture(ResourceLocation texture) {
        this.hoverTexture = texture;
        return this;
    }

    public ButtonWidget hoverTexture(ResourceLocation texture, int u, int v) {
        this.hoverU = u;
        this.hoverV = v;
        return hoverTexture(texture);
    }

    public ButtonWidget hoverTexture(ResourceLocation texture, int u, int v, int uWidth, int vHeight) {
        this.hoverUWidth = uWidth;
        this.hoverVHeight = vHeight;
        return hoverTexture(texture, u, v);
    }

    public ButtonWidget noBackground() {
        this.background = false;
        return this;
    }

    public ButtonWidget tooltip(Component tooltip) {
        this.tooltips.add(tooltip);
        return this;
    }



    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        if(this.background) {
            RenderSystem.setShaderTexture(0, AbstractWidget.WIDGETS_LOCATION);
            int i = this.isMouseOver(mouseX, mouseY) ? 2 : 1;
            AbstractWidget.blit(pose, getX(), getY(), 0, 0, 46 + i * 20, this.width / 2, this.height, 256, 256);
            AbstractWidget.blit(pose, getX() + this.width / 2, getY(), 0, 200 - this.width / 2f, 46 + i * 20, this.width / 2, this.height, 256, 256);
        }
        if(!this.isMouseOver(mouseX, mouseY)) {
            if(this.texture != null) {
                RenderSystem.setShaderTexture(0, this.texture);
                AbstractWidget.blit(pose, getX(), getY(), this.width, this.height, this.u, this.v, this.uWidth, this.vHeight, TextureSizeHelper.getTextureWidth(this.texture), TextureSizeHelper.getTextureHeight(this.texture));
            }
        } else {
            if(this.hoverTexture != null) {
                RenderSystem.setShaderTexture(0, this.hoverTexture);
                AbstractWidget.blit(pose, getX(), getY(), this.width, this.height, this.hoverU, this.hoverV, this.hoverUWidth, this.hoverVHeight, TextureSizeHelper.getTextureWidth(this.hoverTexture), TextureSizeHelper.getTextureHeight(this.hoverTexture));
            } else if(this.texture != null) {
                RenderSystem.setShaderTexture(0, this.texture);
                AbstractWidget.blit(pose, getX(), getY(), this.width, this.height, this.u, this.v, this.uWidth, this.vHeight, TextureSizeHelper.getTextureWidth(this.texture), TextureSizeHelper.getTextureHeight(this.texture));
            }
        }
        if(this.displayTitle && this.title != null && this.title != Component.empty()) {
            Font font = Minecraft.getInstance().font;
            int x = getX() + (this.width - font.width(this.title)) / 2;
            int y = getY() + (this.height - font.lineHeight) / 2;
            font.draw(pose, this.title, x, y, 0);
        }
    }

    @Override
    public List<Component> getTooltips() {
        return this.tooltips;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(this.isMouseOver(mouseX, mouseY) && button == 0) {
            this.onPress.accept(this);
            playDownSound();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}

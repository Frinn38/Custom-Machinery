package fr.frinn.custommachinery.client.screen;

import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement.Orientation;
import fr.frinn.custommachinery.impl.component.config.SideConfig.SpriteData;
import fr.frinn.custommachinery.impl.util.TextureInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

import java.util.Iterator;
import java.util.List;

public class ScreenUtils {

    public static void renderSlotHighlight(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("container/slot_highlight_back"), x - 4, y - 4, width + 8, height + 8);
    }

    public static int getLineHeight() {
        return Minecraft.getInstance().font.lineHeight;
    }

    public static int textWidth(Component text) {
        return Minecraft.getInstance().font.width(text);
    }

    public static void renderOrientedProgressTextures(GuiGraphicsExtractor graphics, TextureInfo emptyTexture, TextureInfo filledTexture, int x, int y, int width, int height, double percent, Orientation orientation) {
        int filledWidth = (int)(width * Mth.clamp(percent, 0.0D, 1.0D));
        int filledHeight = (int)(height * Mth.clamp(percent, 0.0D, 1.0D));

        graphics.blit(RenderPipelines.GUI_TEXTURED, emptyTexture.texture(), x, y, emptyTexture.u(), emptyTexture.v(), width, height, emptyTexture.width(), emptyTexture.height());
        switch (orientation) {
            case RIGHT -> graphics.blit(RenderPipelines.GUI_TEXTURED, filledTexture.texture(), x, y, filledTexture.u(), filledTexture.v(), filledWidth, height, width, height);
            case LEFT -> graphics.blit(RenderPipelines.GUI_TEXTURED, filledTexture.texture(), x + width - filledWidth, y, filledTexture.u() + width - filledWidth, filledTexture.v(), filledWidth, height, width, height);
            case BOTTOM -> graphics.blit(RenderPipelines.GUI_TEXTURED, filledTexture.texture(), x, y, filledTexture.u(), filledTexture.v(), width, filledHeight, width, height);
            case TOP -> graphics.blit(RenderPipelines.GUI_TEXTURED, filledTexture.texture(), x, y + height - filledHeight, filledTexture.u(), filledTexture.v() + height - filledHeight, width, filledHeight, width, height);
        }
    }

    public static void blit(GuiGraphicsExtractor graphics, TextureInfo texture, int x, int y, int width, int height) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture.texture(), x, y, texture.u(), texture.v(), width, height, texture.width(), texture.height());
    }

    public static void drawDottedRect(GuiGraphicsExtractor g, int x, int y, int width, int height, int color, int dashLength, int gapLength, int offset) {
        drawDottedLine(g, x, y, x + width, y, color, dashLength, gapLength, offset); // top
        drawDottedLine(g, x + width, y, x + width, y + height, color, dashLength, gapLength, offset); // right
        drawDottedLine(g, x + width, y + height, x, y + height, color, dashLength, gapLength, offset); // bottom
        drawDottedLine(g, x, y + height, x, y, color, dashLength, gapLength, offset); // left
    }

    public static void drawDottedLine(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int argb, int dashLen, int gapLen, int offset) {
        int total = dashLen + gapLen;

        if (y1 == y2) { // horizontal
            int step = x1 < x2 ? 1 : -1;
            int len = Math.abs(x2 - x1);
            int x = x1;
            for (int i = 0; i < len; i++) {
                int phase = (i + offset) % total;
                if (phase < dashLen) {
                    g.fill(x, y1, x + step, y1 + 1, argb);
                }
                x += step;
            }
        } else if (x1 == x2) { // vertical
            int step = y1 < y2 ? 1 : -1;
            int len = Math.abs(y2 - y1);
            int y = y1;
            for (int i = 0; i < len; i++) {
                int phase = (i + offset) % total;
                if (phase < dashLen) {
                    g.fill(x1, y, x1 + 1, y + step, argb);
                }
                y += step;
            }
        }
    }

    public static void renderScrollingStringNoShadow(GuiGraphicsExtractor guiGraphics, Font font, Component text, int minX, int maxX, int y, int color) {
        int width = font.width(text);
        int maxWidth = maxX - minX;
        if (width > maxWidth) {
            int l = width - maxWidth;
            double d0 = (double) Util.getMillis() / 1000.0;
            double d1 = Math.max((double)l * 0.5, 3.0);
            double d2 = Math.sin((Math.PI / 2) * Math.cos((Math.PI * 2) * d0 / d1)) / 2.0 + 0.5;
            double d3 = Mth.lerp(d2, 0.0, l);
            guiGraphics.enableScissor(minX, y, maxX, y + font.lineHeight);
            guiGraphics.text(font, text, minX - (int)d3, y, color, false);
            guiGraphics.disableScissor();
        } else {
            int i1 = Mth.clamp(minX, minX + width / 2, maxX - width / 2);
            guiGraphics.text(font, text, i1 - font.width(text) / 2, y, color, false);
        }
    }

    public static WidgetSprites dataToSprite(SpriteData data) {
        return new WidgetSprites(data.texture(), data.textureHovered());
    }

    public static Component merge(List<Component> list) {
        MutableComponent main = Component.empty();
        for(Iterator<Component> iterator = list.iterator(); iterator.hasNext();) {
            main.append(iterator.next());
            if(iterator.hasNext())
                main.append("\n");
        }
        return main;
    }
}

package fr.frinn.custommachinery.client.screen;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.common.util.LRU;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.FocusNavigationEvent.ArrowNavigation;
import net.minecraft.client.gui.navigation.FocusNavigationEvent.TabNavigation;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class BaseScreen extends Screen {

    private static final Identifier BLANK_BACKGROUND = CustomMachinery.rl("background");

    public final Minecraft mc = Minecraft.getInstance();

    //Position of the top left corner of the popup.
    public int x;
    public int y;

    //Size of the screen, not same as width/height which is the size of MC windows.
    public int xSize;
    public int ySize;
    private final LRU<PopupScreen> popups = new LRU<>();
    private final Map<PopupScreen, String> popupToId = new HashMap<>();

    private int freezePopupsTicks;

    public BaseScreen(Component component, int xSize, int ySize) {
        super(component);
        this.xSize = xSize;
        this.ySize = ySize;
    }

    public void openPopup(PopupScreen popup) {
        if(this.popups.contains(popup))
            return;
        this.setFocused(null);
        this.popups.add(popup);
        popup.init(this.width, this.height);
    }

    //Prevents opening another popup with same id
    public void openPopup(PopupScreen popup, String id) {
        if(this.popupToId.containsValue(id))
            return;
        this.popupToId.put(popup, id);
        this.openPopup(popup);
        this.freezePopupsTicks = 40;
    }

    public void closePopup(PopupScreen popup) {
        popup.closed();
        this.popups.remove(popup);
        this.popupToId.remove(popup);
    }

    public LRU<PopupScreen> popups() {
        return this.popups;
    }

    @Nullable
    public PopupScreen getPopupUnderMouse(double mouseX, double mouseY) {
        return this.popups.stream()
                .filter(popup -> mouseX >= popup.x && mouseX <= popup.x + popup.xSize && mouseY >= popup.y && mouseY <= popup.y + popup.ySize)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void removed() {
        this.popups.forEach(PopupScreen::closed);
    }

    @Override
    public  <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget) {
        return super.addRenderableWidget(widget);
    }

    @Override
    public void removeWidget(GuiEventListener listener) {
        super.removeWidget(listener);
    }

    @Override
    protected void init() {
        this.x = (this.width - this.xSize) / 2;
        this.y = (this.height - this.ySize) / 2;
        this.popups.forEach(popup -> popup.init(this.width, this.height));
    }

    @Override
    public void tick() {
        this.popups.forEach(PopupScreen::tick);
        if(this.freezePopupsTicks > 0)
            this.freezePopupsTicks--;
    }

    @Override
    public void resize(int width, int height) {
        this.x = (width - this.xSize) / 2;
        this.y = (height - this.ySize) / 2;
        super.resize(width, height);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        PopupScreen hoveredPopup = this.getPopupUnderMouse(mouseX, mouseY);

        graphics.pose().pushMatrix();

        if(hoveredPopup != null)
            super.extractRenderState(graphics, Integer.MAX_VALUE, Integer.MAX_VALUE, partialTicks);
        else
            super.extractRenderState(graphics, mouseX, mouseY, partialTicks);

        for(Iterator<PopupScreen> iterator = this.popups.descendingIterator(); iterator.hasNext();) {
            graphics.nextStratum();
            PopupScreen popup = iterator.next();
            if(hoveredPopup == popup)
                popup.extractRenderStateWithTooltipAndSubtitles(graphics, mouseX, mouseY, partialTicks);
            else
                popup.extractRenderStateWithTooltipAndSubtitles(graphics, Integer.MAX_VALUE, Integer.MAX_VALUE, partialTicks);
        }

        graphics.pose().popMatrix();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        for(PopupScreen popup : this.popups) {
            if(popup.isMouseOver(event.x(), event.y())) {
                boolean clicked = popup.mouseClicked(event, doubleClick);
                if(this.freezePopupsTicks <= 0)
                    this.popups.moveUp(popup);
                return clicked;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        for(PopupScreen popup : this.popups) {
            if(popup.isMouseOver(event.x(), event.y())) {
                boolean released = popup.mouseReleased(event);
                if(this.freezePopupsTicks <= 0)
                    this.popups.moveUp(popup);
                return released;
            }
        }
        this.setDragging(false);
        if(this.getFocused() != null && this.getFocused().mouseReleased(event))
            return true;
        return this.getChildAt(event.x(), event.y()).filter(guiEventListener -> guiEventListener.mouseReleased(event)).isPresent();
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        for(PopupScreen popup : this.popups) {
            boolean dragged = popup.mouseDragged(event, dragX, dragY);
            if(this.freezePopupsTicks <= 0)
                this.popups.moveUp(popup);
            return dragged;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for(PopupScreen popup : this.popups) {
            if(popup.isMouseOver(mouseX, mouseY)) {
                boolean scrolled = popup.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
                if(this.freezePopupsTicks <= 0)
                    this.popups.moveUp(popup);
                return scrolled;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if(event.isEscape()) {
            if(!this.popups.isEmpty()) {
                PopupScreen toClose = this.getPopupUnderMouse(Minecraft.getInstance().mouseHandler.xpos(), Minecraft.getInstance().mouseHandler.ypos());
                if(toClose == null)
                    toClose = this.popups.iterator().next();
                this.closePopup(toClose);
                return true;
            }
            if(this.getFocused() != null && this.getFocused().keyPressed(event))
                return true;
            this.onClose();
            return true;
        }

        for(PopupScreen popup : this.popups) {
            if(popup.keyPressed(event))
                return true;
        }

        if(this.getFocused() != null && this.getFocused().keyPressed(event))
            return true;

        FocusNavigationEvent focusEvent = switch (event.input()) {
            case GLFW.GLFW_KEY_LEFT -> new ArrowNavigation(ScreenDirection.LEFT);
            case GLFW.GLFW_KEY_RIGHT -> new ArrowNavigation(ScreenDirection.RIGHT);
            case GLFW.GLFW_KEY_UP -> new ArrowNavigation(ScreenDirection.UP);
            case GLFW.GLFW_KEY_DOWN -> new ArrowNavigation(ScreenDirection.DOWN);
            case GLFW.GLFW_KEY_TAB -> new TabNavigation(!event.hasShiftDown());
            default -> null;
        };

        if(focusEvent != null) {
            ComponentPath path = this.popups.stream().findFirst().map(popup -> popup.nextFocusPath(focusEvent)).orElse(this.nextFocusPath(focusEvent));
            if (path == null && focusEvent instanceof FocusNavigationEvent.TabNavigation) {
                ComponentPath componentPath = this.getCurrentFocusPath();
                if (componentPath != null)
                    componentPath.applyFocus(false);
                path = super.nextFocusPath(focusEvent);
            }

            if (path != null)
                this.changeFocus(path);

            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        for(PopupScreen popup : this.popups) {
            if(popup.keyReleased(event))
                return true;
        }
        return super.keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        for(PopupScreen popup : this.popups) {
            if(popup.charTyped(event))
                return true;
        }
        return super.charTyped(event);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if(this.getPopupUnderMouse(mouseX, mouseY) != null)
            return false;
        return mouseX >= this.x && mouseX <= this.x + this.xSize && mouseY >= this.y && mouseY <= this.y + this.ySize;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        if(this.getFocused() != focused)
            super.setFocused(focused);
        if(focused != null)
            this.popups.forEach(popup -> popup.setFocused(null));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static void blankBackground(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA, BLANK_BACKGROUND, x, y, width, height);
    }

    public static void drawCenteredScaledString(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, float scale, int color, boolean shadow) {
        graphics.pose().pushMatrix();
        graphics.pose().scale(scale, scale);
        graphics.text(font, text, (int)((x - (font.width(text) * scale) / 2) / scale), (int)((y - font.lineHeight / 2.0f) / scale), color, shadow);
        graphics.pose().popMatrix();
    }

    public static void drawScaledString(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, float scale, int color, boolean shadow) {
        graphics.pose().pushMatrix();
        graphics.pose().scale(scale, scale);
        graphics.text(font, text, (int)(x / scale), (int)(y / scale), color, shadow);
        graphics.pose().popMatrix();
    }

    public static void drawRightAlignedScaledString(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, float scale, int color, boolean shadow) {
        graphics.pose().pushMatrix();
        graphics.pose().scale(scale, scale);
        graphics.text(font, text, (int)((x - font.width(text) * scale) / scale), (int)(y / scale), color, shadow);
        graphics.pose().popMatrix();
    }
}

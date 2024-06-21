package fr.frinn.custommachinery.client.screen;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.common.util.LRU;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.FocusNavigationEvent.ArrowNavigation;
import net.minecraft.client.gui.navigation.FocusNavigationEvent.TabNavigation;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class BaseScreen extends Screen {

    private static final ResourceLocation BLANK_BACKGROUND = CustomMachinery.rl("textures/gui/background.png");

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
        popup.init(Minecraft.getInstance(), this.width, this.height);
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

    public Collection<PopupScreen> popups() {
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
    protected void init() {
        this.x = (this.width - this.xSize) / 2;
        this.y = (this.height - this.ySize) / 2;
        this.popups.forEach(popup -> popup.init(Minecraft.getInstance(), this.width, this.height));
    }

    @Override
    public void tick() {
        this.popups.forEach(PopupScreen::tick);
        if(this.freezePopupsTicks > 0)
            this.freezePopupsTicks--;
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        this.x = (width - this.xSize) / 2;
        this.y = (height - this.ySize) / 2;
        super.resize(minecraft, width, height);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);
        PopupScreen hoveredPopup = this.getPopupUnderMouse(mouseX, mouseY);

        graphics.pose().pushPose();

        if(hoveredPopup != null)
            super.render(graphics, Integer.MAX_VALUE, Integer.MAX_VALUE, partialTicks);
        else
            super.render(graphics, mouseX, mouseY, partialTicks);

        for(Iterator<PopupScreen> iterator = this.popups.descendingIterator(); iterator.hasNext();) {
            graphics.pose().translate(0, 0, 165); //Items are rendered at z=150, tooltips z=400
            PopupScreen popup = iterator.next();
            if(hoveredPopup == popup)
                popup.renderWithTooltip(graphics, mouseX, mouseY, partialTicks);
            else
                popup.render(graphics, Integer.MAX_VALUE, Integer.MAX_VALUE, partialTicks);
        }

        graphics.pose().popPose();

        if(hoveredPopup != null) {
            Tooltip tooltip = hoveredPopup.getTooltip(mouseX, mouseY);
            if(tooltip != null)
                this.setTooltipForNextRenderPass(tooltip, DefaultTooltipPositioner.INSTANCE, true);
            else
                this.deferredTooltipRendering = null;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for(PopupScreen popup : this.popups) {
            if(popup.isMouseOver(mouseX, mouseY)) {
                boolean clicked = popup.mouseClicked(mouseX, mouseY, button);
                if(this.freezePopupsTicks <= 0)
                    this.popups.moveUp(popup);
                return clicked;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for(PopupScreen popup : this.popups) {
            if(popup.isMouseOver(mouseX, mouseY)) {
                boolean released = popup.mouseReleased(mouseX, mouseY, button);
                if(this.freezePopupsTicks <= 0)
                    this.popups.moveUp(popup);
                return released;
            }
        }
        this.setDragging(false);
        if(this.getFocused() != null && this.getFocused().mouseReleased(mouseX, mouseY, button))
            return true;
        return this.getChildAt(mouseX, mouseY).filter(guiEventListener -> guiEventListener.mouseReleased(mouseX, mouseY, button)).isPresent();
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        for(PopupScreen popup : this.popups) {
            if(popup.isMouseOver(mouseX, mouseY)) {
                boolean dragged = popup.mouseDragged(mouseX, mouseY, button, dragX, dragY);
                if(this.freezePopupsTicks <= 0)
                    this.popups.moveUp(popup);
                return dragged;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if(!this.popups.isEmpty()) {
                PopupScreen toClose = this.getPopupUnderMouse(Minecraft.getInstance().mouseHandler.xpos(), Minecraft.getInstance().mouseHandler.ypos());
                if(toClose == null)
                    toClose = this.popups.iterator().next();
                this.closePopup(toClose);
                return true;
            }
            this.onClose();
            return true;
        }

        for(PopupScreen popup : this.popups) {
            if(popup.keyPressed(keyCode, scanCode, modifiers))
                return true;
        }

        if(this.getFocused() != null && this.getFocused().keyPressed(keyCode, scanCode, modifiers))
            return true;

        FocusNavigationEvent event = switch (keyCode) {
            case GLFW.GLFW_KEY_LEFT -> new ArrowNavigation(ScreenDirection.LEFT);
            case GLFW.GLFW_KEY_RIGHT -> new ArrowNavigation(ScreenDirection.RIGHT);
            case GLFW.GLFW_KEY_UP -> new ArrowNavigation(ScreenDirection.UP);
            case GLFW.GLFW_KEY_DOWN -> new ArrowNavigation(ScreenDirection.DOWN);
            case GLFW.GLFW_KEY_TAB -> new TabNavigation(!Screen.hasShiftDown());
            default -> null;
        };

        if(event != null) {
            ComponentPath path = this.popups.stream().findFirst().map(popup -> popup.nextFocusPath(event)).orElse(this.nextFocusPath(event));
            if (path == null && event instanceof FocusNavigationEvent.TabNavigation) {
                ComponentPath componentPath = this.getCurrentFocusPath();
                if (componentPath != null)
                    componentPath.applyFocus(false);
                path = super.nextFocusPath(event);
            }

            if (path != null)
                this.changeFocus(path);

            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for(PopupScreen popup : this.popups) {
            if(popup.keyReleased(keyCode, scanCode, modifiers))
                return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for(PopupScreen popup : this.popups) {
            if(popup.charTyped(codePoint, modifiers))
                return true;
        }
        return super.charTyped(codePoint, modifiers);
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

    public static void blankBackground(GuiGraphics graphics, int x, int y, int width, int height) {
        //Top left
        graphics.blit(BLANK_BACKGROUND, x, y, 0, 0, 4, 4, 8, 8);
        //Top middle
        graphics.blit(BLANK_BACKGROUND, x + 4, y, width - 8, 4, 4, 0, 1, 4, 8, 8);
        //Top right
        graphics.blit(BLANK_BACKGROUND, x + width - 4, y, 4, 0, 4, 3, 8, 8);
        //Middle left
        graphics.blit(BLANK_BACKGROUND, x, y + 4, 4, height - 7, 0, 4, 4, 1, 8, 8);
        //Middle
        graphics.blit(BLANK_BACKGROUND, x + 4, y + 4, width - 7, height - 7, 4, 3, 1, 1, 8, 8);
        //Middle right
        graphics.blit(BLANK_BACKGROUND, x + width - 4, y + 3, 4, height - 7, 4, 3, 4, 1, 8, 8);
        //Bottom left
        graphics.blit(BLANK_BACKGROUND, x, y + height - 3, 0, 5, 4, 3, 8, 8);
        //Bottom middle
        graphics.blit(BLANK_BACKGROUND, x + 4, y + height - 4, width - 8, 4, 3, 4, 1, 4, 8, 8);
        //Bottom right
        graphics.blit(BLANK_BACKGROUND, x + width - 4, y + height - 4, 4, 4, 4, 4, 4, 4, 8, 8);
    }

    public static void drawCenteredString(GuiGraphics graphics, Font font, Component text, int x, int y, int color, boolean shadow) {
        graphics.drawString(font, text, x - font.width(text) / 2, y - font.lineHeight / 2, color, shadow);
    }

    public static void drawCenteredScaledString(GuiGraphics graphics, Font font, Component text, int x, int y, float scale, int color, boolean shadow) {
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 0);
        graphics.drawString(font, text, (int)((x - (font.width(text) * scale) / 2) / scale), (int)((y - font.lineHeight / 2) / scale), color, shadow);
        graphics.pose().popPose();
    }

    public static void drawScaledString(GuiGraphics graphics, Font font, Component text, int x, int y, float scale, int color, boolean shadow) {
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 0);
        graphics.drawString(font, text, (int)(x / scale), (int)(y / scale), color, shadow);
        graphics.pose().popPose();
    }
}

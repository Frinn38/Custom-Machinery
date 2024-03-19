package fr.frinn.custommachinery.client.screen;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.common.util.LRU;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.util.Collection;
import java.util.Iterator;

public abstract class BaseScreen extends Screen {

    private static final ResourceLocation BLANK_BACKGROUND = new ResourceLocation(CustomMachinery.MODID, "textures/gui/background.png");

    public final Minecraft mc = Minecraft.getInstance();

    //Position of the top left corner of the popup.
    public int x;
    public int y;

    //Size of the screen, not same as width/height which is the size of MC windows.
    public int xSize;
    public int ySize;
    private final LRU<PopupScreen> popups = new LRU<>();

    public BaseScreen(Component component, int xSize, int ySize) {
        super(component);
        this.xSize = xSize;
        this.ySize = ySize;
    }

    public void openPopup(PopupScreen popup) {
        if(this.popups.contains(popup))
            return;
        this.popups.add(popup);
        popup.init(Minecraft.getInstance(), this.width, this.height);
    }

    public void closePopup(PopupScreen popup) {
        popup.closed();
        this.popups.remove(popup);
    }

    public Collection<PopupScreen> popups() {
        return this.popups;
    }

    @Override
    public void removed() {
        this.popups.forEach(PopupScreen::closed);
    }

    @Override
    protected void init() {
        this.x = (this.mc.getWindow().getGuiScaledWidth() - this.xSize) / 2;
        this.y = (this.mc.getWindow().getGuiScaledHeight() - this.ySize) / 2;
        this.popups.forEach(popup -> popup.init(Minecraft.getInstance(), this.width, this.height));
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics);
        graphics.pose().pushPose();
        super.render(graphics, mouseX, mouseY, partialTicks);
        for(Iterator<PopupScreen> iterator = this.popups.descendingIterator(); iterator.hasNext();)
            iterator.next().render(graphics, mouseX, mouseY, partialTicks);
        graphics.pose().popPose();
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for(PopupScreen popup : this.popups) {
            if(popup.mouseClicked(mouseX, mouseY, button)) {
                this.popups.moveUp(popup);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for(PopupScreen popup : this.popups) {
            if(popup.mouseReleased(mouseX, mouseY, button))
                return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        for(PopupScreen popup : this.popups) {
            if(popup.mouseDragged(mouseX, mouseY, button, dragX, dragY))
                return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        for(PopupScreen popup : this.popups) {
            if(popup.mouseScrolled(mouseX, mouseY, delta))
                return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        for(PopupScreen popup : this.popups) {
            if(popup.keyPressed(keyCode, scanCode, modifiers))
                return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if(keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        for(PopupScreen popup : this.popups) {
            if(popup.keyReleased(keyCode, scanCode, modifiers))
                return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX <= this.x + this.xSize && mouseY >= this.y && mouseY <= this.y + this.ySize;
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
}

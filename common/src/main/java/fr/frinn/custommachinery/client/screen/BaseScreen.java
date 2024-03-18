package fr.frinn.custommachinery.client.screen;

import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class BaseScreen extends Screen {

    private static final ResourceLocation BLANK_BACKGROUND = new ResourceLocation(CustomMachinery.MODID, "textures/gui/background.png");

    private final List<Pair<DragType, Rectangle>> draggingAreas = new ArrayList<>();
    public final int xSize;
    public final int ySize;

    private int xPos;
    private int yPos;
    private double xOffset = 0;
    private double yOffset = 0;
    private double widthOffset = 0;
    private double heightOffset = 0;
    private DragType currentDragType = DragType.NONE;

    @Nullable
    private PopupScreen popup = null;

    public BaseScreen(Component component, int xSize, int ySize) {
        super(component);
        this.xSize = xSize;
        this.ySize = ySize;
    }

    public int getX() {
        return this.xPos + (int)this.xOffset;
    }

    public int getY() {
        return this.yPos + (int)this.yOffset;
    }

    public int getWidth() {
        return this.xSize + (int)this.widthOffset;
    }

    public int getHeight() {
        return this.ySize + (int)this.heightOffset;
    }

    public void openPopup(PopupScreen popup) {
        this.popup = popup;
        this.popup.setParent(this);
        this.popup.init(Minecraft.getInstance(), this.width, this.height);
    }

    public void closePopup() {
        this.popup = null;
    }

    public void addDraggingArea(DragType type, Rectangle rectangle) {
        this.draggingAreas.add(Pair.of(type, rectangle));
    }

    public DragType getDragType(double mouseX, double mouseY) {
        for(Pair<DragType, Rectangle> pair : this.draggingAreas) {
            if(pair.getSecond().isIn(mouseX, mouseY))
                return pair.getFirst();
        }
        return DragType.NONE;
    }

    public void baseMoveDraggingArea() {
        addDraggingArea(DragType.MOVE, new Rectangle(() -> this.getX() + 2, () -> this.getX() + this.getWidth() - 2, () -> this.getY() + 2, () -> this.getY() + 10));
    }

    public void baseSizeDraggingArea(int margin) {
        addDraggingArea(DragType.TOP, new Rectangle(() -> this.getX() + margin, () -> this.getX() + this.getWidth() - margin, () -> this.getY() - margin, () -> this.getY() + margin));
        addDraggingArea(DragType.TOP_LEFT, new Rectangle(() -> this.getX() - margin, () -> this.getX() + margin, () -> this.getY() - margin, () -> this.getY() + margin));
        addDraggingArea(DragType.TOP_RIGHT, new Rectangle(() -> this.getX() + this.getWidth() - margin, () -> this.getX() + this.getWidth() + margin, () -> this.getY() - margin, () -> this.getY() + margin));
        addDraggingArea(DragType.LEFT, new Rectangle(() -> this.getX() - margin, () -> this.getX() + margin, () -> this.getY() + margin, () -> this.getY() + this.getHeight() - margin));
        addDraggingArea(DragType.RIGHT, new Rectangle(() -> this.getX() + this.getWidth() - margin, () -> this.getX() + this.getWidth() + margin, () -> this.getY() + margin, () -> this.getY() + this.getHeight() - margin));
        addDraggingArea(DragType.BOTTOM, new Rectangle(() -> this.getX() + margin, () -> this.getX() + this.getWidth() - margin, () -> this.getY() + this.getHeight() - margin, () -> this.getY() + this.getHeight() + margin));
        addDraggingArea(DragType.BOTTOM_LEFT, new Rectangle(() -> this.getX() - margin, () -> this.getX() + margin, () -> this.getY() + this.getHeight() - margin, () -> this.getY() + this.getHeight() + margin));
        addDraggingArea(DragType.BOTTOM_RIGHT, new Rectangle(() -> this.getX() + this.getWidth() - margin, () -> this.getX() + this.getWidth() + margin, () -> this.getY() + this.getHeight() - margin, () -> this.getY() + this.getHeight() + margin));
    }

    @Override
    protected void init() {
        this.draggingAreas.clear();
        this.xPos = (this.width - this.xSize) / 2;
        this.yPos = (this.height - this.ySize) / 2;
        if(this.popup != null)
            this.popup.init(Minecraft.getInstance(), this.width, this.height);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics);
        graphics.pose().pushPose();
        graphics.pose().translate(this.xOffset, this.yOffset, 0);
        super.render(graphics, mouseX, mouseY, partialTicks);
        if(this.popup != null)
            this.popup.render(graphics, mouseX, mouseY, partialTicks);
        graphics.pose().popPose();
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(this.popup != null)
            return this.popup.mouseClicked(mouseX, mouseY, button);
        if(super.mouseClicked(mouseX, mouseY, button))
            return true;
        this.currentDragType = getDragType(mouseX, mouseY);
        if(this.currentDragType != DragType.NONE)
            setDragging(true);
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.setDragging(false);
        this.currentDragType = DragType.NONE;
        if(this.popup != null)
            return this.popup.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if(this.popup != null)
            return this.popup.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        if(this.isDragging() && button == 0 && this.currentDragType != DragType.NONE) {
            switch(this.currentDragType) {
                case MOVE -> {
                    this.xOffset += dragX;
                    this.yOffset += dragY;
                }
                case TOP -> {
                    this.yOffset = Math.min(0, this.yOffset + dragY);
                    this.heightOffset = Math.max(0, this.heightOffset - dragY);
                }
                case BOTTOM -> this.heightOffset = Math.max(0, this.heightOffset + dragY);
                case LEFT -> {
                    this.widthOffset = Math.max(0, this.widthOffset - dragX);
                    if(this.widthOffset > 0)
                        this.xOffset = Math.min(0, this.xOffset + dragX);
                }
                case RIGHT -> this.widthOffset = Math.max(0, this.widthOffset + dragX);
                case TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT -> {
                    this.widthOffset += dragX;
                    this.heightOffset += dragY;
                }
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if(this.popup != null)
            return this.popup.mouseScrolled(mouseX, mouseY, delta);
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        if(this.popup != null)
            return this.popup.keyPressed(keyCode, scanCode, modifiers);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if(keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        if(this.popup != null)
            return this.popup.keyReleased(keyCode, scanCode, modifiers);
        return super.keyReleased(keyCode, scanCode, modifiers);
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

    public enum DragType {
        MOVE,
        TOP_LEFT,
        TOP,
        TOP_RIGHT,
        LEFT,
        RIGHT,
        BOTTOM_LEFT,
        BOTTOM,
        BOTTOM_RIGHT,
        NONE
    }

    public static class Rectangle {

        private final Supplier<Integer> left;
        private final Supplier<Integer> right;
        private final Supplier<Integer> top;
        private final Supplier<Integer> bottom;

        public Rectangle(Supplier<Integer> left, Supplier<Integer> right, Supplier<Integer> top, Supplier<Integer> bottom) {
            this.left = left;
            this.right = right;
            this.top = top;
            this.bottom = bottom;
        }

        public boolean isIn(double x, double y) {
            return x >= this.left.get() && x <= this.right.get() && y >= this.top.get() && y <= this.bottom.get();
        }

        public void render(GuiGraphics graphics, int color) {
            graphics.fill(this.left.get(), this.top.get(), this.right.get(), this.bottom.get(), color);
        }
    }
}

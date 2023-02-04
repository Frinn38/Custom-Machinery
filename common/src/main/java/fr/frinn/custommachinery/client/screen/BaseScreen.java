package fr.frinn.custommachinery.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.client.screen.widget.custom.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class BaseScreen extends Screen {

    private static final ResourceLocation BLANK_BACKGROUND = new ResourceLocation(CustomMachinery.MODID, "textures/gui/background.png");

    private final List<Widget> customWidgets = new ArrayList<>();
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

    public <T extends Widget> T addCustomWidget(T widget) {
        this.customWidgets.add(widget);
        return widget;
    }

    @Override
    protected <T extends GuiEventListener & NarratableEntry> T addWidget(T listener) {
        throw new IllegalStateException("Forbidden method");
    }

    @Override
    protected <T extends net.minecraft.client.gui.components.Widget> T addRenderableOnly(T widget) {
        throw new IllegalStateException("Forbidden method");
    }

    @Override
    protected <T extends GuiEventListener & net.minecraft.client.gui.components.Widget & NarratableEntry> T addRenderableWidget(T widget) {
        throw new IllegalStateException("Forbidden method");
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
        this.customWidgets.clear();
        this.draggingAreas.clear();
        this.xPos = (this.width - this.xSize) / 2;
        this.yPos = (this.height - this.ySize) / 2;
        if(this.popup != null)
            this.popup.init(Minecraft.getInstance(), this.width, this.height);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(poseStack);
        if(false)
            GuiDebugUtils.showDragAreas(poseStack, this.draggingAreas);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.customWidgets.forEach(widget -> widget.render(poseStack, mouseX, mouseY, partialTicks));
        if(this.popup != null) {
            this.popup.render(poseStack, mouseX, mouseY, partialTicks);
            return;
        }
        this.customWidgets.stream().filter(widget -> widget.isMouseOver(mouseX, mouseY)).forEach(widget -> renderTooltip(poseStack, widget.getTooltips(), Optional.empty(), mouseX, mouseY));
    }

    @Override
    public void renderBackground(PoseStack pose) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(this.popup != null)
            return this.popup.mouseClicked(mouseX, mouseY, button);
        for(Widget widget : this.customWidgets)
            if(widget.isMouseOver(mouseX, mouseY) && widget.mouseClicked(mouseX, mouseY, button))
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
        for(Widget widget : this.customWidgets)
            if(widget.isMouseOver(mouseX, mouseY) && widget.mouseReleased(mouseX, mouseY, button))
                return true;
        return false;
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
            return true;
        }
        for(Widget widget : this.customWidgets)
            if(widget.isMouseOver(mouseX, mouseY) && widget.mouseDragged(mouseX, mouseY, button, dragX, dragY))
                return true;
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if(this.popup != null)
            return this.popup.mouseScrolled(mouseX, mouseY, delta);
        for(Widget widget : this.customWidgets)
            if(widget.isMouseOver(mouseX, mouseY) && widget.mouseScrolled(mouseX, mouseY, delta))
                return true;
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        if(this.popup != null)
            return this.popup.keyPressed(keyCode, scanCode, modifiers);
        for(Widget widget : this.customWidgets)
            if(widget.keyPressed(keyCode, scanCode, modifiers))
                return true;
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if(keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        if(this.popup != null)
            return this.popup.keyReleased(keyCode, scanCode, modifiers);
        for(Widget widget : this.customWidgets)
            if(widget.keyReleased(keyCode, scanCode, modifiers))
                return true;
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static void baseBackground(PoseStack pose, int x, int y, int width, int height) {
        GuiComponent.fillGradient(pose, x, y, width, height, -1072689136, -804253680, 0);
    }

    public static void blankBackground(PoseStack pose, int x, int y, int width, int height) {
        bindTexture(BLANK_BACKGROUND);
        //Top left
        blit(pose, x, y, 0, 0, 4, 4, 8, 8);
        //Top middle
        blit(pose, x + 4, y, width - 8, 4, 4, 0, 1, 4, 8, 8);
        //Top right
        blit(pose, x + width - 4, y, 4, 0, 4, 3, 8, 8);
        //Middle left
        blit(pose, x, y + 4, 4, height - 7, 0, 4, 4, 1, 8, 8);
        //Middle
        blit(pose, x + 4, y + 4, width - 7, height - 7, 4, 3, 1, 1, 8, 8);
        //Middle right
        blit(pose, x + width - 4, y + 3, 4, height - 7, 4, 3, 4, 1, 8, 8);
        //Bottom left
        blit(pose, x, y + height - 3, 0, 5, 4, 3, 8, 8);
        //Bottom middle
        blit(pose, x + 4, y + height - 4, width - 8, 4, 3, 4, 1, 4, 8, 8);
        //Bottom right
        blit(pose, x + width - 4, y + height - 4, 4, 4, 4, 4, 4, 4, 8, 8);
    }

    public static void bindTexture(ResourceLocation texture) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
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

        public void render(PoseStack pose, int color) {
            GuiComponent.fill(pose, this.left.get(), this.top.get(), this.right.get(), this.bottom.get(), color);
        }
    }
}

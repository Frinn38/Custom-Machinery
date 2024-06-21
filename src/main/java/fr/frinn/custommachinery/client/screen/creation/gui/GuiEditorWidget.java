package fr.frinn.custommachinery.client.screen.creation.gui;

import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.api.machine.ICustomMachine;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.popup.ConfirmPopup;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Comparators;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;
import fr.frinn.custommachinery.impl.guielement.GuiElementWidgetSupplierRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class GuiEditorWidget extends AbstractWidget implements ContainerEventHandler {

    private final MachineEditScreen parent;
    private final IMachineScreen dummyScreen = new DummyScreen();
    private final List<IGuiElement> elements = new ArrayList<>();
    private final List<WidgetEditorWidget<?>> widgets = new ArrayList<>();
    private final Button config;
    private final Button priorityUp;
    private final Button priorityDown;
    private final Button delete;

    private boolean dragging;
    private GuiEventListener focused;

    public GuiEditorWidget(MachineEditScreen parent, int x, int y, int width, int height, List<IGuiElement> baseElements) {
        super(x, y, width, height, Component.empty());
        this.parent = parent;
        baseElements.stream().sorted(Comparators.GUI_ELEMENTS_COMPARATOR.reversed()).forEach(this::addElement);
        this.config = Button.builder(Component.empty(), button -> {
            if(this.getFocused() instanceof WidgetEditorWidget<?> widget)
                this.config(widget);
        }).size(5, 5).tooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.config"))).build();
        this.priorityUp = Button.builder(Component.empty(), button -> this.changePriority(1)).size(5, 5).build();
        this.priorityDown = Button.builder(Component.empty(), button -> this.changePriority(-1)).size(5, 5).build();
        this.delete = Button.builder(Component.empty(), button -> this.delete()).size(5, 5).tooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.delete"))).build();
    }

    public void addElement(IGuiElement element) {
        if(!GuiElementWidgetSupplierRegistry.hasWidgetSupplier(element.getType()) || !GuiElementBuilderRegistry.hasBuilder(element.getType()))
            return;

        this.elements.add(element);
        this.widgets.add(this.getWidget(element));
    }

    public void addCreatedElement(IGuiElement element) {
        if(!GuiElementWidgetSupplierRegistry.hasWidgetSupplier(element.getType()) || !GuiElementBuilderRegistry.hasBuilder(element.getType()))
            return;

        this.elements.add(element);
        WidgetEditorWidget<?> widget = this.getWidget(element);
        widget.setPosition(this.getX() + (this.getWidth() + widget.getWidth()) / 2, this.getY() + (this.getHeight() + widget.getHeight()) / 2);
        this.widgets.add(widget);
        this.setFocused(widget);
    }

    public void hideButtons() {
        this.config.visible = false;
        this.priorityUp.visible = false;
        this.priorityDown.visible = false;
        this.delete.visible = false;
    }

    public void showButtons(WidgetEditorWidget<?> widget) {
        this.config.setPosition(widget.getX() - 1, widget.getY() - 7);
        this.config.visible = true;
        this.priorityUp.setPosition(widget.getX() + 5, widget.getY() - 7);
        this.priorityUp.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.priorityUp").append("\n").append(Component.translatable("custommachinery.gui.creation.gui.priority.value", widget.properties.getPriority()).withStyle(ChatFormatting.GRAY))));
        this.priorityUp.visible = true;
        this.priorityDown.setPosition(widget.getX() + 11, widget.getY() - 7);
        this.priorityDown.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.priorityDown").append("\n").append(Component.translatable("custommachinery.gui.creation.gui.priority.value", widget.properties.getPriority()).withStyle(ChatFormatting.GRAY))));
        this.priorityDown.visible = true;
        this.delete.setPosition(widget.getX() + 17, widget.getY() - 7);
        this.delete.visible = true;
    }

    public <T extends IGuiElement> void config(WidgetEditorWidget<T> widget) {
        this.parent.openPopup(widget.builder.makeConfigPopup(this.parent, widget.properties, widget.widget.getElement(), widget::refreshWidget));
    }

    private void changePriority(int delta) {
        if(this.getFocused() instanceof WidgetEditorWidget<?> widget) {
            widget.properties.setPriority(widget.properties.getPriority() + delta);
            widget.refreshWidget(null);
            List<WidgetEditorWidget<?>> sorted = this.widgets.stream().sorted(Comparator.comparingInt(w -> w.properties.getPriority())).toList();
            this.widgets.clear();
            this.widgets.addAll(sorted);
            this.priorityUp.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.priorityUp").append("\n").append(Component.translatable("custommachinery.gui.creation.gui.priority.value", widget.properties.getPriority()).withStyle(ChatFormatting.GRAY))));
            this.priorityDown.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.priorityDown").append("\n").append(Component.translatable("custommachinery.gui.creation.gui.priority.value", widget.properties.getPriority()).withStyle(ChatFormatting.GRAY))));
            this.parent.setChanged();
        }
    }

    private void delete() {
        if(this.getFocused() instanceof WidgetEditorWidget<?> widget) {
            ConfirmPopup popup = new ConfirmPopup(this.parent, 128, 96, () -> {
                this.widgets.remove(widget);
                this.setFocused(null);
                this.parent.getBuilder().getGuiElements().remove(widget.widget.getElement());
                this.parent.setChanged();
            });
            popup.title(Component.translatable("custommachinery.gui.popup.warning").withStyle(ChatFormatting.DARK_RED));
            popup.text(Component.translatable("custommachinery.gui.creation.gui.delete.popup"));
            this.parent.openPopup(popup);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends IGuiElement> WidgetEditorWidget<T> getWidget(T element) {
        AbstractGuiElementWidget<T> widget =  GuiElementWidgetSupplierRegistry.getWidgetSupplier((GuiElementType<T>)element.getType()).get(element, this.dummyScreen);
        IGuiElementBuilder<T> builder = GuiElementBuilderRegistry.getBuilder((GuiElementType<T>)element.getType());
        return new WidgetEditorWidget<>(widget, builder);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(this.getX() - 2, this.getY() - 2, this.getX() + this.getWidth() + 2, this.getY() + this.getHeight() + 2, FastColor.ARGB32.color(255, 0, 0, 0));
        graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), FastColor.ARGB32.color(255, 198, 198, 198));
        graphics.pose().pushPose();
        this.widgets.forEach(widget -> widget.render(graphics, mouseX, mouseY, partialTick));
        graphics.pose().popPose();
        this.config.render(graphics, mouseX, mouseY, partialTick);
        this.priorityUp.render(graphics, mouseX, mouseY, partialTick);
        this.priorityDown.render(graphics, mouseX, mouseY, partialTick);
        this.delete.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public void setX(int x) {
        super.setX(x);
        this.widgets.forEach(widget -> widget.setX(x + widget.widget.getElement().getX()));
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        this.widgets.forEach(widget -> widget.setY(y + widget.widget.getElement().getY()));
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.widgets;
    }

    @Override
    public boolean isDragging() {
        return this.dragging;
    }

    @Override
    public void setDragging(boolean isDragging) {
        this.dragging = isDragging;
    }

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return this.focused;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        if(this.focused != null)
            this.focused.setFocused(false);
        this.focused = focused;
        if(focused != null)
            focused.setFocused(true);
        if(focused instanceof WidgetEditorWidget<?> widget)
            this.showButtons(widget);
        else
            this.hideButtons();
    }

    public Optional<GuiEventListener> getChildAt(double mouseX, double mouseY) {
        for (GuiEventListener guiEventListener : this.children()) {
            if (!guiEventListener.isMouseOver(mouseX, mouseY)) continue;
            return Optional.of(guiEventListener);
        }
        return Optional.empty();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(this.config.mouseClicked(mouseX, mouseY, button))
            return true;
        else if(this.priorityUp.mouseClicked(mouseX, mouseY, button))
            return true;
        else if(this.priorityDown.mouseClicked(mouseX, mouseY, button))
            return true;
        else if(this.delete.mouseClicked(mouseX, mouseY, button))
            return true;

        for (GuiEventListener guiEventListener : this.children()) {
            if (!guiEventListener.mouseClicked(mouseX, mouseY, button)) continue;
            this.setFocused(guiEventListener);
            if (button == 0)
                this.setDragging(true);
            return true;
        }
        this.setFocused(null);
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.setDragging(false);
        return this.getFocused() != null && this.getFocused().mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.getFocused() != null && this.isDragging() && button == 0)
            return this.getFocused().mouseDragged(mouseX, mouseY, button, dragX, dragY);
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return this.getChildAt(mouseX, mouseY).filter(arg -> arg.mouseScrolled(mouseX, mouseY, scrollX, scrollY)).isPresent();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.getFocused() != null && this.getFocused().keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return this.getFocused() != null && this.getFocused().keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return this.getFocused() != null && this.getFocused().charTyped(codePoint, modifiers);
    }

    public class WidgetEditorWidget<T extends IGuiElement> extends AbstractWidget {

        private final IGuiElementBuilder<T> builder;
        private final MutableProperties properties;

        private AbstractGuiElementWidget<T> widget;
        private DragType dragType = DragType.DEFAULT;
        private double dragX = 0.0D;
        private double dragY = 0.0D;

        public WidgetEditorWidget(AbstractGuiElementWidget<T> widget, IGuiElementBuilder<T> builder) {
            super(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(), widget.getMessage());
            this.widget = widget;
            this.builder = builder;
            this.properties = new MutableProperties(widget.getElement().getProperties());
        }

        @SuppressWarnings("unchecked")
        public void refreshWidget(@Nullable T from) {
            T element = this.widget.getElement();
            T newElement = from != null ? from : this.builder.make(this.properties.build(), element);
            this.widget = GuiElementWidgetSupplierRegistry.getWidgetSupplier((GuiElementType<T>)element.getType()).get(newElement, GuiEditorWidget.this.dummyScreen);
            this.widget.setPosition(this.getX(), this.getY());
            this.width = this.widget.getWidth();
            this.height = this.widget.getHeight();
            GuiEditorWidget.this.parent.getBuilder().getGuiElements().remove(element);
            GuiEditorWidget.this.parent.getBuilder().getGuiElements().add(newElement);
        }

        private DragType getDragType(double mouseX, double mouseY) {
            if(!this.isMouseOver(mouseX, mouseY))
                return DragType.DEFAULT;

            //Left
            if(mouseX >= this.getX() && mouseX <= this.getX() + 1 && mouseY >= this.getY() && mouseY <= this.getY() + this.getHeight())
                return DragType.LEFT_RESIZE;
            //Right
            else if(mouseX >= this.getX() + this.getWidth() - 1 && mouseX <= this.getX() + this.getWidth() && mouseY >= this.getY() && mouseY <= this.getY() + this.getHeight())
                return DragType.RIGHT_RESIZE;
            //Top
            else if(mouseX >= this.getX() && mouseX <= this.getX() + this.getWidth() && mouseY >= this.getY() && mouseY <= this.getY() + 1)
                return DragType.UP_RESIZE;
            //Bottom
            else if(mouseX >= this.getX() && mouseX <= this.getX() + this.getWidth() && mouseY >= this.getY() + this.getHeight() - 1 && mouseY <= this.getY() + this.getHeight())
                return DragType.DOWN_RESIZE;
            //Default
            else
                return DragType.DEFAULT;
        }

        private void checkCursorShape(int mouseX, int mouseY) {
            if(this.dragType != DragType.DEFAULT)
                return;
            switch (this.getDragType(mouseX, mouseY)) {
                case LEFT_RESIZE, RIGHT_RESIZE -> GLFW.glfwSetCursor(Minecraft.getInstance().getWindow().getWindow(), GLFW.glfwCreateStandardCursor(GLFW.GLFW_HRESIZE_CURSOR));
                case UP_RESIZE, DOWN_RESIZE -> GLFW.glfwSetCursor(Minecraft.getInstance().getWindow().getWindow(), GLFW.glfwCreateStandardCursor(GLFW.GLFW_VRESIZE_CURSOR));
                default -> GLFW.glfwSetCursor(Minecraft.getInstance().getWindow().getWindow(), GLFW.glfwCreateStandardCursor(GLFW.GLFW_CURSOR));
            }
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.pose().pushPose();
            switch (this.dragType) {
                case DEFAULT -> graphics.pose().translate(this.dragX, this.dragY, 0);
                case LEFT_RESIZE -> {
                    graphics.pose().translate(-this.getX() * -this.dragX / this.getWidth() + this.dragX, 0, 0);
                    graphics.pose().scale((float)(-this.dragX / this.getWidth()) + 1, 1.0F, 1.0F);
                }
                case RIGHT_RESIZE -> {
                    graphics.pose().translate(-this.getX() * this.dragX / this.getWidth(), 0, 0);
                    graphics.pose().scale((float)(this.dragX / this.getWidth()) + 1, 1.0F, 1.0F);
                }
                case UP_RESIZE -> {
                    graphics.pose().translate(0, -this.getY() * -this.dragY / this.getHeight() + this.dragY, 0);
                    graphics.pose().scale(1.0F, (float)(-this.dragY / this.getHeight()) + 1, 1.0F);
                }
                case DOWN_RESIZE -> {
                    graphics.pose().translate(0, -this.getY() * this.dragY / this.getHeight(), 0);
                    graphics.pose().scale(1.0F, (float)(this.dragY / this.getHeight()) + 1, 1.0F);
                }
            }
            if(this.isFocused()) {
                graphics.fill(this.getX() -1, this.getY() - 1, this.getX() + this.getWidth() + 1, this.getY() + this.getHeight() + 1, FastColor.ARGB32.color(255, 255, 0, 0));
                graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), FastColor.ARGB32.color(255, 198, 198, 198));
                checkCursorShape(mouseX, mouseY);
            }
            this.widget.render(graphics, Integer.MAX_VALUE, Integer.MAX_VALUE, partialTick);
            graphics.pose().popPose();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }

        @Override
        public void setX(int x) {
            super.setX(x);
            this.properties.setX(x - GuiEditorWidget.this.getX());
            this.refreshWidget(null);
        }

        @Override
        public void setY(int y) {
            super.setY(y);
            this.properties.setY(y - GuiEditorWidget.this.getY());
            this.refreshWidget(null);
        }

        @Override
        public void setWidth(int width) {
            super.setWidth(width);
            this.properties.setWidth(width);
            this.refreshWidget(null);
        }

        public void setHeight(int height) {
            this.height = height;
            this.properties.setHeight(height);
            this.refreshWidget(null);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            this.dragType = this.getDragType(mouseX, mouseY);
        }

        @Override
        protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
            GuiEditorWidget.this.hideButtons();

            switch (this.dragType) {
                case DEFAULT, LEFT_RESIZE, UP_RESIZE -> {
                    this.dragX = Mth.clamp(this.dragX + dragX, GuiEditorWidget.this.getX() - this.getX(), GuiEditorWidget.this.getX() + GuiEditorWidget.this.getWidth() - this.getX() - this.getWidth());
                    this.dragY = Mth.clamp(this.dragY + dragY, GuiEditorWidget.this.getY() - this.getY(), GuiEditorWidget.this.getY() + GuiEditorWidget.this.getHeight() - this.getY() - this.getHeight());
                }
                case RIGHT_RESIZE -> this.dragX = Mth.clamp(this.dragX + dragX, -this.getWidth(), GuiEditorWidget.this.getX() + GuiEditorWidget.this.getWidth() - this.getX() - this.getWidth());
                case DOWN_RESIZE -> this.dragY = Mth.clamp(this.dragY + dragY, -this.getHeight(), GuiEditorWidget.this.getY() + GuiEditorWidget.this.getHeight() - this.getY() - this.getHeight());
            }
        }

        @Override
        public void onRelease(double mouseX, double mouseY) {
            if(this.dragX == 0.0D && this.dragY == 0.0D)
                return;

            switch (this.dragType) {
                case DEFAULT -> {
                    this.setX(this.getX() + (int)this.dragX);
                    this.setY(this.getY() + (int)this.dragY);
                }
                case LEFT_RESIZE -> {
                    this.setX(this.getX() + (int)this.dragX);
                    this.setWidth(this.getWidth() - (int)this.dragX);
                }
                case RIGHT_RESIZE -> this.setWidth(this.getWidth() + (int)this.dragX);
                case UP_RESIZE -> {
                    this.setY(this.getY() + (int)this.dragY);
                    this.setHeight(this.getHeight() - (int)this.dragY);
                }
                case DOWN_RESIZE -> this.setHeight(this.getHeight() + (int)this.dragY);
            }

            GuiEditorWidget.this.showButtons(this);
            GuiEditorWidget.this.parent.setChanged();

            this.dragType = DragType.DEFAULT;
            this.dragX = 0.0D;
            this.dragY = 0.0D;
            GLFW.glfwSetCursor(Minecraft.getInstance().getWindow().getWindow(), GLFW.glfwCreateStandardCursor(GLFW.GLFW_CURSOR));
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            int move = Screen.hasShiftDown() ? 5 : Screen.hasControlDown() ? 10 : 1;
            return switch (keyCode) {
                case GLFW.GLFW_KEY_LEFT -> {
                    this.setX(Math.max(this.getX() - move, GuiEditorWidget.this.getX()));
                    yield true;
                }
                case GLFW.GLFW_KEY_RIGHT -> {
                    this.setX(Math.min(this.getX() + move, GuiEditorWidget.this.getX() + GuiEditorWidget.this.getWidth()));
                    yield true;
                }
                case GLFW.GLFW_KEY_UP -> {
                    this.setY(Math.max(this.getY() - move, GuiEditorWidget.this.getY()));
                    yield true;
                }
                case GLFW.GLFW_KEY_DOWN -> {
                    this.setY(Math.min(this.getY() + move, GuiEditorWidget.this.getY() + GuiEditorWidget.this.getHeight()));
                    yield true;
                }
                case GLFW.GLFW_KEY_DELETE -> {
                    GuiEditorWidget.this.delete();
                    yield true;
                }
                default -> false;
            };
        }
    }

    private class DummyScreen implements IMachineScreen {

        private final MachineTile dummy = new CustomMachineTile(BlockPos.ZERO, Registration.CUSTOM_MACHINE_BLOCK.get().defaultBlockState());

        @Override
        public int getX() {
            return GuiEditorWidget.this.getX();
        }

        @Override
        public int getY() {
            return GuiEditorWidget.this.getY();
        }

        @Override
        public int getWidth() {
            return GuiEditorWidget.this.getWidth();
        }

        @Override
        public int getHeight() {
            return GuiEditorWidget.this.getHeight();
        }

        @Override
        public MachineTile getTile() {
            return this.dummy;
        }

        @Override
        public ICustomMachine getMachine() {
            return this.dummy.getMachine();
        }
    }

    private enum DragType {
        DEFAULT,
        UP_RESIZE,
        DOWN_RESIZE,
        LEFT_RESIZE,
        RIGHT_RESIZE
    }
}

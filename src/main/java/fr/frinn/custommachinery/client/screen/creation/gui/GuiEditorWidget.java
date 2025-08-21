package fr.frinn.custommachinery.client.screen.creation.gui;

import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.api.machine.ICustomMachine;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.tabs.GuiTab;
import fr.frinn.custommachinery.client.screen.popup.ConfirmPopup;
import fr.frinn.custommachinery.common.guielement.BackgroundGuiElement;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Comparators;
import fr.frinn.custommachinery.common.util.Utils;
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
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class GuiEditorWidget extends AbstractWidget implements ContainerEventHandler {

    private final MachineEditScreen parent;
    private final IMachineScreen dummyScreen = new DummyScreen();
    private final List<WidgetEditorWidget<?>> widgets = new ArrayList<>();
    private final Deque<Change> changes = new ArrayDeque<>();
    private final List<WidgetEditorWidget<?>> selected = new ArrayList<>();
    private final List<WidgetEditorWidget<?>> copied = new ArrayList<>();
    private final Button config;
    private final Button priorityUp;
    private final Button priorityDown;
    private final Button delete;

    private boolean dragging = false;
    private boolean pasting = false;
    private Vector2d selectionBoxStart;
    private GuiEventListener focused;

    private static GridSettings gridSettings = new GridSettings(false, 10, 10, 0.5F);
    private static boolean showBackground = true;

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
        this.hideButtons();
    }

    public void addElement(IGuiElement element) {
        if(!GuiElementWidgetSupplierRegistry.hasWidgetSupplier(element.getType()) || !GuiElementBuilderRegistry.hasBuilder(element.getType()))
            return;

        this.widgets.add(this.getWidget(element));
    }

    public void addCreatedElement(IGuiElement element) {
        if(!GuiElementWidgetSupplierRegistry.hasWidgetSupplier(element.getType()) || !GuiElementBuilderRegistry.hasBuilder(element.getType()))
            return;

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

    public GridSettings getGridSettings() {
        return gridSettings;
    }

    public void setGridSettings(GridSettings settings) {
        gridSettings = settings;
    }

    public boolean shouldShowBackground() {
        return showBackground;
    }

    public void setShowBackground(boolean show) {
        showBackground = show;
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
                this.memorizeChange(new SingleWidgetChange(widget));
                this.copied.remove(widget);
                this.widgets.remove(widget);
                this.setFocused(null);
                this.parent.getBuilder().getGuiElements().remove(widget.widget.getElement());
                this.parent.setChanged();
            });
            popup.title(Component.translatable("custommachinery.gui.popup.warning").withStyle(ChatFormatting.DARK_RED));
            popup.text(Component.translatable("custommachinery.gui.creation.gui.delete.popup"));
            popup.cancelCallback(() -> this.parent.setFocused(this));
            this.parent.openPopup(popup);
        }
        if(!this.selected.isEmpty()) {
            ConfirmPopup popup = new ConfirmPopup(this.parent, 128, 96, () -> {
                GroupWidgetChange change = this.memorizeChange(new GroupWidgetChange());
                for(Iterator<WidgetEditorWidget<?>> iterator = this.selected.iterator(); iterator.hasNext(); ) {
                    WidgetEditorWidget<?> widget = iterator.next();
                    change.add(widget);
                    this.copied.remove(widget);
                    this.widgets.remove(widget);
                    this.parent.getBuilder().getGuiElements().remove(widget.widget.getElement());
                    iterator.remove();
                }
                this.parent.setChanged();
            });
            popup.title(Component.translatable("custommachinery.gui.popup.warning").withStyle(ChatFormatting.DARK_RED));
            popup.text(Component.translatable("custommachinery.gui.creation.gui.delete.popup"));
            popup.cancelCallback(() -> this.parent.setFocused(this));
            this.parent.openPopup(popup);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends IGuiElement> WidgetEditorWidget<T> getWidget(T element) {
        AbstractGuiElementWidget<T> widget =  GuiElementWidgetSupplierRegistry.getWidgetSupplier((GuiElementType<T>)element.getType()).get(element, this.dummyScreen);
        IGuiElementBuilder<T> builder = GuiElementBuilderRegistry.getBuilder((GuiElementType<T>)element.getType());
        return new WidgetEditorWidget<>(widget, builder);
    }

    public <C extends Change> C memorizeChange(C change) {
        this.changes.addFirst(change);
        if(this.parent.getTabManager().getCurrentTab() instanceof GuiTab guiTab && guiTab.revert != null)
            guiTab.revert.active = true;
        return change;
    }

    public void revertChange() {
        if(this.changes.isEmpty())
            return;
        this.changes.getFirst().revert();
        this.changes.removeFirst();
        if(this.parent.getTabManager().getCurrentTab() instanceof GuiTab guiTab && guiTab.revert != null)
            guiTab.revert.active = !this.changes.isEmpty();
    }

    public void copy() {
        this.copied.clear();
        if(this.focused instanceof WidgetEditorWidget<?> widget)
            this.copied.add(widget);
        else if(!this.selected.isEmpty())
            this.copied.addAll(this.selected);
        if(this.parent.getTabManager().getCurrentTab() instanceof GuiTab guiTab && guiTab.paste != null)
            guiTab.paste.active = !this.copied.isEmpty();

    }

    public void paste() {
        if(!this.copied.isEmpty())
            this.pasting = true;
    }

    public void selectAll() {
        this.selected.clear();
        this.selected.addAll(this.widgets);
    }

    public void alignTop() {
        if(this.selected.isEmpty())
            return;
        int y = this.selected.stream().mapToInt(AbstractWidget::getY).min().orElse(0);
        GroupWidgetChange change = this.memorizeChange(new GroupWidgetChange());
        this.selected.forEach(widget -> {
            change.add(widget);
            widget.setY(y);
        });
    }

    public void alignCenter() {
        if(this.selected.isEmpty())
            return;
        int y = (int)this.selected.stream().mapToDouble(widget -> widget.getY() + widget.getHeight() / 2.0D).average().orElse(0);
        GroupWidgetChange change = this.memorizeChange(new GroupWidgetChange());
        this.selected.forEach(widget -> {
            change.add(widget);
            widget.setY(y - (int)(widget.getHeight() / 2.0));
        });
    }

    public void alignBottom() {
        if(this.selected.isEmpty())
            return;
        int y = this.selected.stream().mapToInt(widget -> widget.getY() + widget.getHeight()).max().orElse(0);
        GroupWidgetChange change = this.memorizeChange(new GroupWidgetChange());
        this.selected.forEach(widget -> {
            change.add(widget);
            widget.setY(y - widget.getHeight());
        });
    }

    public void alignLeft() {
        if(this.selected.isEmpty())
            return;
        int x = this.selected.stream().mapToInt(AbstractWidget::getX).min().orElse(0);
        GroupWidgetChange change = this.memorizeChange(new GroupWidgetChange());
        this.selected.forEach(widget -> {
            change.add(widget);
            widget.setX(x);
        });
    }

    public void alignMiddle() {
        if(this.selected.isEmpty())
            return;
        int x = (int)this.selected.stream().mapToDouble(widget -> widget.getX() + widget.getWidth() / 2.0).min().orElse(0);
        GroupWidgetChange change = this.memorizeChange(new GroupWidgetChange());
        this.selected.forEach(widget -> {
            change.add(widget);
            widget.setX(x - (int)(widget.getWidth() / 2.0));
        });
    }

    public void alignRight() {
        if(this.selected.isEmpty())
            return;
        int x = this.selected.stream().mapToInt(widget -> widget.getX() + widget.getWidth()).max().orElse(0);
        GroupWidgetChange change = this.memorizeChange(new GroupWidgetChange());
        this.selected.forEach(widget -> {
            change.add(widget);
            widget.setX(x - widget.getWidth());
        });

    }

    public void centerHorizontally() {
        if(this.focused instanceof WidgetEditorWidget<?> widget) {
            this.memorizeChange(new SingleWidgetChange(widget));
            widget.setX(this.getX() + this.getWidth() / 2 - widget.getWidth() / 2);
            this.showButtons(widget);
        } else if(!this.selected.isEmpty()) {
            GroupWidgetChange change = this.memorizeChange(new GroupWidgetChange());
            int minX = this.selected.stream().mapToInt(AbstractWidget::getX).min().orElse(0);
            int maxX = this.selected.stream().mapToInt(widget -> widget.getX() + widget.getWidth()).max().orElse(0);
            this.selected.forEach(widget -> {
                change.add(widget);
                widget.setX(this.getX() + this.getWidth() / 2 - (maxX - minX) / 2 + widget.getX() - minX);
            });
        }
    }

    public void centerVertically() {
        if(this.focused instanceof WidgetEditorWidget<?> widget) {
            this.memorizeChange(new SingleWidgetChange(widget));
            widget.setY(this.getY() + this.getHeight() / 2 - widget.getHeight() / 2);
            this.showButtons(widget);
        } else if(!this.selected.isEmpty()) {
            GroupWidgetChange change = this.memorizeChange(new GroupWidgetChange());
            int minY = this.selected.stream().mapToInt(AbstractWidget::getY).min().orElse(0);
            int maxY = this.selected.stream().mapToInt(widget -> widget.getY() + widget.getHeight()).max().orElse(0);
            this.selected.forEach(widget -> {
                change.add(widget);
                widget.setY(this.getY() + this.getHeight() / 2 - (maxY - minY) / 2 + widget.getY() - minY);
            });
        }
    }

    public void compact() {
        if(this.selected.isEmpty())
            return;

        GroupWidgetChange change = this.memorizeChange(new GroupWidgetChange());
        WidgetCompactor.compact(new ArrayList<>(this.selected), change::add, 15, 1);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        //Black border
        graphics.fill(this.getX() - 2, this.getY() - 2, this.getX() + this.getWidth() + 2, this.getY() + this.getHeight() + 2, FastColor.ARGB32.color(255, 0, 0, 0));
        graphics.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.getWidth() + 1, this.getY() + this.getHeight() + 1, FastColor.ARGB32.color(255, 198, 198, 198));

        //Background
        if(this.shouldShowBackground()) {
            BackgroundGuiElement background = this.parent.getBuilder().getGuiElements().stream().filter(element -> element instanceof BackgroundGuiElement).map(element -> (BackgroundGuiElement)element).findFirst().orElse(null);
            if(background != null && background.getTexture() != null)
                ClientHandler.blit(graphics, background.getTexture(), this.getX(), this.getY(), this.width, this.height);
        }

        //Grid
        if(this.getGridSettings() != null && this.getGridSettings().enabled()) {
            for(int x = this.getX() + this.getGridSettings().xSpacing(); x < this.getX() + this.getWidth(); x += this.getGridSettings().xSpacing())
                graphics.fill(x, this.getY(), x + 1, this.getY() + this.getHeight(), FastColor.ARGB32.color((int)(255 * this.getGridSettings().opacity()), 85, 85, 85));

            for(int y = this.getY() + this.getGridSettings().ySpacing(); y < this.getY() + this.getHeight(); y += this.getGridSettings().ySpacing())
                graphics.fill(this.getX(), y, this.getX() + this.getWidth(), y + 1, FastColor.ARGB32.color((int)(255 * this.getGridSettings().opacity()), 85, 85, 85));
        }

        //Selection box
        if(this.selectionBoxStart != null && this.isDragging())
            graphics.renderOutline((int)Math.min(this.selectionBoxStart.x(), mouseX), (int)Math.min(this.selectionBoxStart.y(), mouseY), (int)Math.abs(this.selectionBoxStart.x() - mouseX), (int)Math.abs(this.selectionBoxStart.y() - mouseY), FastColor.ARGB32.color(255, 0, 0, 255));
        if(this.parent.getTabManager().getCurrentTab() instanceof GuiTab guiTab) {
            guiTab.enableAlignButtons(!this.selected.isEmpty());
            if(guiTab.copy != null)
                guiTab.copy.active = this.focused != null || !this.selected.isEmpty();
            if(guiTab.centerHorizontally != null)
                guiTab.centerHorizontally.active = this.focused != null || !this.selected.isEmpty();
            if(guiTab.centerVertically != null)
                guiTab.centerVertically.active = this.focused != null || !this.selected.isEmpty();
        }

        //Elements
        this.widgets.forEach(widget -> widget.render(graphics, mouseX, mouseY, partialTick));

        //Buttons
        this.config.render(graphics, mouseX, mouseY, partialTick);
        this.priorityUp.render(graphics, mouseX, mouseY, partialTick);
        this.priorityDown.render(graphics, mouseX, mouseY, partialTick);
        this.delete.render(graphics, mouseX, mouseY, partialTick);

        //Pasting cursor
        if(this.pasting && this.isMouseOver(mouseX, mouseY))
            GLFW.glfwSetCursor(Minecraft.getInstance().getWindow().getWindow(), GLFW.glfwCreateStandardCursor(GLFW.GLFW_POINTING_HAND_CURSOR));

        //Mouse coordinates
        if(this.isMouseOver(mouseX, mouseY)) {
            Component text = Component.translatable("custommachinery.gui.creation.gui.mouse", Math.clamp(mouseX - this.getX(), 0, this.getWidth()), Math.clamp(mouseY - this.getY(), 0, this.getHeight()));
            graphics.drawString(Minecraft.getInstance().font, text, this.getX() + this.getWidth() - Minecraft.getInstance().font.width(text), this.getY() + this.getHeight() + 2, 0, false);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public void setX(int x) {
        super.setX(x);
        this.widgets.forEach(widget -> widget.setX(x + widget.widget.getElement().getX()));
        if(this.getFocused() instanceof WidgetEditorWidget<?> widget) {
            this.hideButtons();
            this.showButtons(widget);
        }
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        this.widgets.forEach(widget -> widget.setY(y + widget.widget.getElement().getY()));
        if(this.getFocused() instanceof WidgetEditorWidget<?> widget) {
            this.hideButtons();
            this.showButtons(widget);
        }
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
        if(Screen.hasControlDown() && focused instanceof WidgetEditorWidget<?> widget) {
            this.selected.add(widget);
            if(this.focused != null) {
                this.focused.setFocused(false);
                if(this.focused instanceof WidgetEditorWidget<?> focusedWidget)
                    this.selected.add(focusedWidget);
            }
            this.focused = null;
            return;
        }
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
        if(!this.isMouseOver(mouseX, mouseY) || button != 0)
            return false;
        if(this.pasting && !this.copied.isEmpty()) {
            this.pasting = false;
            int minX = this.copied.stream().mapToInt(WidgetEditorWidget::getX).min().orElse(0);
            int minY = this.copied.stream().mapToInt(WidgetEditorWidget::getY).min().orElse(0);
            GroupWidgetChange change = this.memorizeChange(new GroupWidgetChange());
            this.copied.forEach(widget -> {
                WidgetEditorWidget<?> copy = widget.copy();
                copy.setPosition((int)(mouseX + widget.getX() - minX), (int)(mouseY + widget.getY() - minY));
                this.widgets.add(copy);
                change.add(new AddedWidgetChange(copy));
            });
            this.copied.clear();
            if(this.parent.getTabManager().getCurrentTab() instanceof GuiTab guiTab && guiTab.paste != null)
                guiTab.paste.active = false;
            this.selected.clear();
            this.setFocused(null);
            GLFW.glfwSetCursor(Minecraft.getInstance().getWindow().getWindow(), GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR));
            return true;
        }
        if(this.config.mouseClicked(mouseX, mouseY, button))
            return true;
        else if(this.priorityUp.mouseClicked(mouseX, mouseY, button))
            return true;
        else if(this.priorityDown.mouseClicked(mouseX, mouseY, button))
            return true;
        else if(this.delete.mouseClicked(mouseX, mouseY, button))
            return true;

        for(GuiEventListener guiEventListener : this.widgets.reversed()) {
            if(!guiEventListener.mouseClicked(mouseX, mouseY, button))
                continue;
            if(guiEventListener instanceof WidgetEditorWidget<?> widget && this.selected.contains(widget)) {
                this.setDragging(true);
                return true;
            }
            this.setFocused(guiEventListener);
            this.setDragging(true);
            this.selected.clear();
            return true;
        }
        this.setFocused(null);
        this.setDragging(true);
        this.selected.clear();
        this.selectionBoxStart = new Vector2d(mouseX, mouseY);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.setDragging(false);
        if(this.selectionBoxStart != null) {
            Rect2i selectionBox = new Rect2i((int)Math.min(this.selectionBoxStart.x(), mouseX), (int)Math.min(this.selectionBoxStart.y(), mouseY), (int)Math.abs(this.selectionBoxStart.x() - mouseX), (int)Math.abs(this.selectionBoxStart.y() - mouseY));
            this.widgets.stream()
                    .filter(widget -> ClientHandler.isOverlapping(selectionBox, new Rect2i(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight())))
                    .sorted(Comparator.comparingInt(widget -> widget.getX() * 1000 + widget.getY()))
                    .forEach(this.selected::add);
            this.selectionBoxStart = null;
        }
        if(this.getFocused() != null)
            return this.getFocused().mouseReleased(mouseX, mouseY, button);
        else if(!this.selected.isEmpty()) {
            GroupWidgetChange change = new GroupWidgetChange();
            this.selected.forEach(widget -> {
                widget.mouseReleased(mouseX, mouseY, button);
                if(!this.changes.isEmpty() && this.changes.getFirst() instanceof SingleWidgetChange singleChange && singleChange.widget == widget)
                    change.add(this.changes.removeFirst());
            });
            if(change.hasChanges())
                this.memorizeChange(change);
            this.hideButtons();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if(this.isDragging() && button == 0) {
            if(this.getFocused() != null)
                return this.getFocused().mouseDragged(mouseX, mouseY, button, dragX, dragY);
            else if(!this.selected.isEmpty()) {
                this.selected.forEach(widget -> {
                    widget.dragType = DragType.DEFAULT;
                    widget.mouseDragged(mouseX, mouseY, button, dragX, dragY);
                });
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return this.getChildAt(mouseX, mouseY).filter(arg -> arg.mouseScrolled(mouseX, mouseY, scrollX, scrollY)).isPresent();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        String key = GLFW.glfwGetKeyName(keyCode, scanCode);
        if(key != null && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            switch(key) {
                case "z" -> this.revertChange();
                case "c" -> this.copy();
                case "v" -> this.paste();
                case "a" -> this.selectAll();
            }
            return true;
        }

        if(!this.selected.isEmpty()) {
            int move = Screen.hasShiftDown() ? 5 : Screen.hasControlDown() ? 10 : 1;
            GroupWidgetChange change = new GroupWidgetChange();
            for(WidgetEditorWidget<?> widget : this.selected) {
                boolean moved =  switch (keyCode) {
                    case GLFW.GLFW_KEY_LEFT -> {
                        change.add(widget);
                        widget.setX(Math.max(widget.getX() - move, this.getX()));
                        yield true;
                    }
                    case GLFW.GLFW_KEY_RIGHT -> {
                        change.add(widget);
                        widget.setX(Math.min(widget.getX() + move, this.getX() + this.getWidth()));
                        yield true;
                    }
                    case GLFW.GLFW_KEY_UP -> {
                        change.add(widget);
                        widget.setY(Math.max(widget.getY() - move, this.getY()));
                        yield true;
                    }
                    case GLFW.GLFW_KEY_DOWN -> {
                        change.add(widget);
                        widget.setY(Math.min(widget.getY() + move, this.getY() + this.getHeight()));
                        yield true;
                    }
                    default -> false;
                };
                if(moved)
                    GuiEditorWidget.this.parent.setChanged();
            }
            if(keyCode == GLFW.GLFW_KEY_DELETE)
                this.delete();
            if(change.hasChanges())
                this.memorizeChange(change);
            return true;
        }
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
        private DragType dragType = DragType.NONE;
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

        public WidgetEditorWidget<T> copy() {
            MutableProperties copyProperties = new MutableProperties(this.properties.build());
            String id = this.properties.getId();
            AtomicReference<String> copyId = new AtomicReference<>(Utils.incrementLastNumber(id));
            //Check if there isn't another element with this id.
            while(GuiEditorWidget.this.widgets.stream().anyMatch(widget -> {
                if(widget == this || widget.builder.type() != this.builder.type())
                    return false;
                return widget.properties.getId().equals(copyId.get());
            })) {
                copyId.set(Utils.incrementLastNumber(copyId.get()));
            }
            copyProperties.setId(copyId.get());
            T element = this.builder.make(copyProperties.build(), this.widget.getElement());
            return GuiEditorWidget.this.getWidget(element);
        }

        private DragType getDragType(double mouseX, double mouseY) {
            if(GuiEditorWidget.this.selected.stream().anyMatch(widget -> widget.isMouseOver(mouseX, mouseY)))
                return DragType.DEFAULT;
            if(!this.isMouseOver(mouseX, mouseY))
                return DragType.NONE;

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
            if(this.dragType != DragType.NONE)
                return;
            switch (this.getDragType(mouseX, mouseY)) {
                case LEFT_RESIZE, RIGHT_RESIZE -> GLFW.glfwSetCursor(Minecraft.getInstance().getWindow().getWindow(), GLFW.glfwCreateStandardCursor(GLFW.GLFW_RESIZE_EW_CURSOR));
                case UP_RESIZE, DOWN_RESIZE -> GLFW.glfwSetCursor(Minecraft.getInstance().getWindow().getWindow(), GLFW.glfwCreateStandardCursor(GLFW.GLFW_RESIZE_NS_CURSOR));
                case DEFAULT -> GLFW.glfwSetCursor(Minecraft.getInstance().getWindow().getWindow(), GLFW.glfwCreateStandardCursor(GLFW.GLFW_RESIZE_ALL_CURSOR));
                default -> GLFW.glfwSetCursor(Minecraft.getInstance().getWindow().getWindow(), GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR));
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
            boolean highlighted = false;
            if(this.isFocused()) {
                graphics.fill(this.getX() -1, this.getY() - 1, this.getX() + this.getWidth() + 1, this.getY() + this.getHeight() + 1, FastColor.ARGB32.color(255, 255, 0, 0));
                graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), FastColor.ARGB32.color(255, 198, 198, 198));
                checkCursorShape(mouseX, mouseY);
                highlighted = true;
            } else if(GuiEditorWidget.this.selected.contains(this)) {
                graphics.fill(this.getX() -1, this.getY() - 1, this.getX() + this.getWidth() + 1, this.getY() + this.getHeight() + 1, FastColor.ARGB32.color(255, 0, 0, 255));
                graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), FastColor.ARGB32.color(255, 198, 198, 198));
                checkCursorShape(mouseX, mouseY);
                highlighted = true;
            }
            this.widget.render(graphics, Integer.MAX_VALUE, Integer.MAX_VALUE, partialTick);
            if(GuiEditorWidget.this.copied.contains(this)) {
                int offset = (int)(System.currentTimeMillis() / 100 % 100);
                if(highlighted)
                    ClientHandler.drawDottedRect(graphics, this.getX() - 2, this.getY() - 2, this.getWidth() + 3, this.getHeight() + 3, FastColor.ARGB32.color(255, 0, 255, 0), 4, 4, offset);
                else
                    ClientHandler.drawDottedRect(graphics, this.getX() - 1, this.getY() - 1, this.getWidth() + 1, this.getHeight() + 1, FastColor.ARGB32.color(255, 0, 255, 0), 4, 4, offset);
            }
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
        public void onClick(double mouseX, double mouseY, int button) {
            this.dragType = this.getDragType(mouseX, mouseY);
            checkCursorShape((int)mouseX, (int)mouseY);
        }

        @Override
        protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
            GuiEditorWidget.this.hideButtons();

            switch (this.dragType) {
                case DEFAULT -> {
                    if(GuiEditorWidget.this.focused == this) {
                        this.dragX = Mth.clamp(this.dragX + dragX, GuiEditorWidget.this.getX() - this.getX(), GuiEditorWidget.this.getX() + GuiEditorWidget.this.getWidth() - this.getX() - this.getWidth());
                        this.dragY = Mth.clamp(this.dragY + dragY, GuiEditorWidget.this.getY() - this.getY(), GuiEditorWidget.this.getY() + GuiEditorWidget.this.getHeight() - this.getY() - this.getHeight());
                    } else if(GuiEditorWidget.this.selected.contains(this)) {
                        double minDragX = GuiEditorWidget.this.selected.stream().mapToDouble(widget -> widget.getX() - widget.getWidth()).max().orElse(0);
                        double minDragY = GuiEditorWidget.this.selected.stream().mapToDouble(widget -> widget.getY() - widget.getHeight()).max().orElse(0);
                        double maxDragX = GuiEditorWidget.this.selected.stream().mapToDouble(widget -> widget.getX() + widget.getWidth()).max().orElse(0);
                        double maxDragY = GuiEditorWidget.this.selected.stream().mapToDouble(widget -> widget.getY() + widget.getHeight()).max().orElse(0);
                        this.dragX = Mth.clamp(this.dragX + dragX, GuiEditorWidget.this.getX() - minDragX, GuiEditorWidget.this.getX() + GuiEditorWidget.this.getWidth() - maxDragX);
                        this.dragY = Mth.clamp(this.dragY + dragY, GuiEditorWidget.this.getY() - minDragY, GuiEditorWidget.this.getY() + GuiEditorWidget.this.getHeight() - maxDragY);
                    }
                }
                 case LEFT_RESIZE, UP_RESIZE -> {
                    this.dragX = Mth.clamp(this.dragX + dragX, GuiEditorWidget.this.getX() - this.getX(), GuiEditorWidget.this.getX() + GuiEditorWidget.this.getWidth() - this.getX() - this.getWidth());
                    this.dragY = Mth.clamp(this.dragY + dragY, GuiEditorWidget.this.getY() - this.getY(), GuiEditorWidget.this.getY() + GuiEditorWidget.this.getHeight() - this.getY() - this.getHeight());
                }
                case RIGHT_RESIZE -> this.dragX = Mth.clamp(this.dragX + dragX, -this.getWidth(), GuiEditorWidget.this.getX() + GuiEditorWidget.this.getWidth() - this.getX() - this.getWidth());
                case DOWN_RESIZE -> this.dragY = Mth.clamp(this.dragY + dragY, -this.getHeight(), GuiEditorWidget.this.getY() + GuiEditorWidget.this.getHeight() - this.getY() - this.getHeight());
            }
        }

        @Override
        public void onRelease(double mouseX, double mouseY) {
            if(this.dragX == 0.0D && this.dragY == 0.0D) {
                this.dragType = DragType.NONE;
                return;
            }

            switch (this.dragType) {
                case DEFAULT -> {
                    GuiEditorWidget.this.memorizeChange(new SingleWidgetChange(this));
                    this.setX(this.getX() + (int)this.dragX);
                    this.setY(this.getY() + (int)this.dragY);
                }
                case LEFT_RESIZE -> {
                    GuiEditorWidget.this.memorizeChange(new SingleWidgetChange(this));
                    this.setX(this.getX() + (int)this.dragX);
                    this.setWidth(this.getWidth() - (int)this.dragX);
                }
                case RIGHT_RESIZE -> {
                    GuiEditorWidget.this.memorizeChange(new SingleWidgetChange(this));
                    this.setWidth(this.getWidth() + (int)this.dragX);
                }
                case UP_RESIZE -> {
                    GuiEditorWidget.this.memorizeChange(new SingleWidgetChange(this));
                    this.setY(this.getY() + (int)this.dragY);
                    this.setHeight(this.getHeight() - (int)this.dragY);
                }
                case DOWN_RESIZE -> {
                    GuiEditorWidget.this.memorizeChange(new SingleWidgetChange(this));
                    this.setHeight(this.getHeight() + (int)this.dragY);
                }
            }

            GuiEditorWidget.this.showButtons(this);
            GuiEditorWidget.this.parent.setChanged();

            this.dragType = DragType.NONE;
            this.dragX = 0.0D;
            this.dragY = 0.0D;
            GLFW.glfwSetCursor(Minecraft.getInstance().getWindow().getWindow(), GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR));
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            int move = Screen.hasShiftDown() ? 5 : Screen.hasControlDown() ? 10 : 1;
            boolean moved =  switch (keyCode) {
                case GLFW.GLFW_KEY_LEFT -> {
                    GuiEditorWidget.this.memorizeChange(new SingleWidgetChange(this));
                    this.setX(Math.max(this.getX() - move, GuiEditorWidget.this.getX()));
                    yield true;
                }
                case GLFW.GLFW_KEY_RIGHT -> {
                    GuiEditorWidget.this.memorizeChange(new SingleWidgetChange(this));
                    this.setX(Math.min(this.getX() + move, GuiEditorWidget.this.getX() + GuiEditorWidget.this.getWidth()));
                    yield true;
                }
                case GLFW.GLFW_KEY_UP -> {
                    GuiEditorWidget.this.memorizeChange(new SingleWidgetChange(this));
                    this.setY(Math.max(this.getY() - move, GuiEditorWidget.this.getY()));
                    yield true;
                }
                case GLFW.GLFW_KEY_DOWN -> {
                    GuiEditorWidget.this.memorizeChange(new SingleWidgetChange(this));
                    this.setY(Math.min(this.getY() + move, GuiEditorWidget.this.getY() + GuiEditorWidget.this.getHeight()));
                    yield true;
                }
                case GLFW.GLFW_KEY_DELETE -> {
                    GuiEditorWidget.this.delete();
                    yield true;
                }
                default -> false;
            };
            if(moved) {
                GuiEditorWidget.this.parent.setChanged();
                if(this.isFocused())
                    GuiEditorWidget.this.showButtons(this);
            }
            return moved;
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
        NONE,//No drag
        DEFAULT,//Dragging
        UP_RESIZE,
        DOWN_RESIZE,
        LEFT_RESIZE,
        RIGHT_RESIZE
    }

    public record GridSettings(boolean enabled, int xSpacing, int ySpacing, float opacity) {}

    public interface Change {
        void revert();
    }

    public class SingleWidgetChange implements Change {

        private final WidgetEditorWidget<?> widget;
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        public SingleWidgetChange(WidgetEditorWidget<?> widget) {
            this.widget = widget;
            this.x = widget.getX();
            this.y = widget.getY();
            this.width = widget.getWidth();
            this.height = widget.getHeight();
        }

        public SingleWidgetChange(WidgetEditorWidget<?> widget, int x, int y, int width, int height) {
            this.widget = widget;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        @Override
        public void revert() {
            if(!GuiEditorWidget.this.widgets.contains(this.widget)) {
                GuiEditorWidget.this.widgets.add(this.widget);
                this.widget.refreshWidget(null);
            } else {
                if(this.widget.getX() != this.x)
                    this.widget.setX(this.x);
                if(this.widget.getY() != this.y)
                    this.widget.setY(this.y);
                if(this.widget.getWidth() != this.width)
                    this.widget.setWidth(this.width);
                if(this.widget.getHeight() != this.height)
                    this.widget.setHeight(this.height);
            }
            GuiEditorWidget.this.showButtons(this.widget);
        }
    }

    public class GroupWidgetChange implements Change {
        private final List<Change> changes;

        public GroupWidgetChange(List<Change> changes) {
            this.changes = changes;
        }

        public GroupWidgetChange() {
            this.changes = new ArrayList<>();
        }

        public void add(Change change) {
            this.changes.add(change);
        }

        public void add(WidgetEditorWidget<?> widget) {
            this.changes.add(new SingleWidgetChange(widget));
        }

        public boolean hasChanges() {
            return !this.changes.isEmpty();
        }

        @Override
        public void revert() {
            this.changes.forEach(Change::revert);
            GuiEditorWidget.this.setFocused(null);
        }
    }

    public class AddedWidgetChange implements Change {

        private final WidgetEditorWidget<?> widget;

        private AddedWidgetChange(WidgetEditorWidget<?> widget) {
            this.widget = widget;
        }

        @Override
        public void revert() {
            GuiEditorWidget.this.copied.remove(widget);
            GuiEditorWidget.this.widgets.remove(widget);
            GuiEditorWidget.this.parent.getBuilder().getGuiElements().remove(widget.widget.getElement());
            GuiEditorWidget.this.parent.setChanged();
        }
    }
}

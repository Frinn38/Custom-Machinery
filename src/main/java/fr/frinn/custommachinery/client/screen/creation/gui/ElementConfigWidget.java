package fr.frinn.custommachinery.client.screen.creation.gui;

import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiEditorWidget.SingleWidgetChange;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiEditorWidget.WidgetEditorWidget;
import fr.frinn.custommachinery.client.screen.widget.GroupWidget;
import fr.frinn.custommachinery.client.screen.widget.IntegerEditBox;
import fr.frinn.custommachinery.client.screen.widget.IntegerSlider;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;

public class ElementConfigWidget extends GroupWidget {

    private final GuiEditorWidget parent;
    private final StringWidget title;
    private final Button config;
    private final IntegerSlider priority;
    private final EditBox id;
    private final IntegerEditBox xPos;
    private final IntegerEditBox yPos;
    private final IntegerEditBox width;
    private final IntegerEditBox height;
    private final Button delete;
    private WidgetEditorWidget<?> widget = null;

    public ElementConfigWidget(int x, int y, int width, int height, GuiEditorWidget parent) {
        super(x, y, width, height, Component.empty());
        this.parent = parent;
        GridLayout layout = new GridLayout(this.getX(), this.getY());
        layout.defaultCellSetting().paddingTop(4).paddingHorizontal(5);
        RowHelper row = layout.createRowHelper(2);
        LayoutSettings tag = layout.newCellSettings().alignHorizontallyLeft().alignVerticallyMiddle();
        LayoutSettings right = layout.newCellSettings().alignHorizontallyRight();

        //Title
        this.title = row.addChild(new StringWidget(width - 10, Minecraft.getInstance().font.lineHeight, Component.literal("Dummy"), Minecraft.getInstance().font), 2);

        //Config
        this.config = row.addChild(Button.builder(Component.translatable("custommachinery.gui.creation.gui.config"), button -> {
            if(this.widget != null)
                this.parent.config(widget);
        }).size(80, 20).tooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.config"))).build(), 2);

        //Priority
        this.priority = row.addChild(IntegerSlider.builder().bounds(-10, 10).create(0, 0, 80, 20, Component.translatable("custommachinery.gui.creation.gui.priority")), 2);

        //Id
        this.id = row.addChild(new EditBox(Minecraft.getInstance().font, 80, 20, Component.empty()), 2);

        //X pos
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.x"), Minecraft.getInstance().font), tag);
        this.xPos = row.addChild(new IntegerEditBox(Minecraft.getInstance().font, 0, 0, 30, 20, Component.empty()), right);
        this.xPos.bounds(0, this.parent.getWidth());

        //Y pos
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.y"), Minecraft.getInstance().font), tag);
        this.yPos = row.addChild(new IntegerEditBox(Minecraft.getInstance().font, 0, 0, 30, 20, Component.empty()), right);
        this.yPos.bounds(0, this.parent.getHeight());

        //Width
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.width"), Minecraft.getInstance().font), tag);
        this.width = row.addChild(new IntegerEditBox(Minecraft.getInstance().font, 0, 0, 30, 20, Component.empty()), right);
        this.width.bounds(1, this.parent.getWidth());

        //Height
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.height"), Minecraft.getInstance().font), tag);
        this.height = row.addChild(new IntegerEditBox(Minecraft.getInstance().font, 0, 0, 30, 20, Component.empty()), right);
        this.height.bounds(1, this.parent.getHeight());

        //Delete
        this.delete = row.addChild(Button.builder(Component.translatable("custommachinery.gui.creation.delete").withStyle(ChatFormatting.RED), button -> this.parent.delete())
                .size(80, 20).tooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.delete"))).build(), 2);

        layout.arrangeElements();
        layout.visitWidgets(this::addWidget);
        super.width = layout.getWidth();
        super.height = layout.getHeight() + 5;
    }

    public void show(WidgetEditorWidget<?> widget) {
        this.hide();//reset all EditBox/Sliders
        this.widget = widget;
        this.visible = true;
        this.active = true;

        this.title.setMessage(widget.getBuilder().type().getTranslatedName());
        this.priority.setValue(widget.getProperties().getPriority());
        this.priority.setResponder(priority -> this.parent.changePriority(priority - widget.getProperties().getPriority()));
        this.id.setValue(widget.getProperties().getId());
        this.id.moveCursorToStart(false);
        this.id.moveCursorToEnd(false);
        this.id.setResponder(id -> {
            widget.getProperties().setId(id);
            widget.refreshWidget(null);
            this.parent.setChanged();
            if(this.parent.getWidgets(widget.getBuilder().type(), id).size() > 1) {
                this.id.setTextColor(FastColor.ARGB32.color(255, 255, 0, 0));
                this.id.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.id.duplicate", id)));
            } else {
                this.id.setTextColor(FastColor.ARGB32.color(255, 255, 255, 255));
                this.id.setTooltip(null);
            }
        });
        this.xPos.setIntValue(widget.getX() - this.parent.getX());
        this.xPos.setIntResponder(x -> {
            if(x + this.parent.getX() == widget.getX()) return;
            this.parent.memorizeChange(widget);
            widget.setX(x + this.parent.getX());
            this.parent.setChanged();
        });
        this.yPos.setIntValue(widget.getY() - this.parent.getY());
        this.yPos.setIntResponder(y -> {
            if(y + this.parent.getY() == widget.getY()) return;
            this.parent.memorizeChange(widget);
            widget.setY(y + this.parent.getY());
            this.parent.setChanged();
        });
        this.width.setIntValue(widget.getWidth());
        this.width.setIntResponder(width -> {
            if(width == widget.getWidth()) return;
            this.parent.memorizeChange(widget);
            widget.setWidth(width);
            this.parent.setChanged();
        });
        this.height.setIntValue(widget.getHeight());
        this.height.setIntResponder(height -> {
            if(height == widget.getHeight()) return;
            this.parent.memorizeChange(widget);
            widget.setHeight(height);
            this.parent.setChanged();
        });
    }

    public void hide() {
        this.widget = null;
        this.visible = false;
        this.active = false;

        this.priority.setResponder(priority -> {});
        this.id.setResponder(id -> {});
        this.xPos.setIntResponder(x -> {});
        this.yPos.setIntResponder(y -> {});
        this.width.setIntResponder(width -> {});
        this.height.setIntResponder(height -> {});
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if(!this.visible)
            return;
        BaseScreen.blankBackground(graphics, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
    }
}

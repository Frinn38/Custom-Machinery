package fr.frinn.custommachinery.client.screen.creation.tabs;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.gui.BackgroundEditorPopup;
import fr.frinn.custommachinery.client.screen.creation.gui.GridEditorPopup;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiEditorWidget;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiElementCreationPopup;
import fr.frinn.custommachinery.common.guielement.BackgroundGuiElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.network.chat.Component;

import java.util.List;

public class GuiTab extends MachineEditTab {

    public static final WidgetSprites CREATE_SPRITES = new WidgetSprites(CustomMachinery.rl("creation/create_button"), CustomMachinery.rl("creation/create_button_hovered"));
    public static final WidgetSprites BACKGROUND_SPRITES = new WidgetSprites(CustomMachinery.rl("creation/background_button"), CustomMachinery.rl("creation/background_button_hovered"));
    public static final WidgetSprites GRID_SPRITES = new WidgetSprites(CustomMachinery.rl("creation/grid_button"), CustomMachinery.rl("creation/grid_button_hovered"));
    public static final WidgetSprites REVERT_SPRITES = new WidgetSprites(CustomMachinery.rl("creation/revert_button"), CustomMachinery.rl("creation/revert_button_disabled"), CustomMachinery.rl("creation/revert_button_hovered"), CustomMachinery.rl("creation/revert_button_disabled_hovered"));
    public static final WidgetSprites COPY_SPRITES = new WidgetSprites(CustomMachinery.rl("creation/copy_button"), CustomMachinery.rl("creation/copy_button_disabled"), CustomMachinery.rl("creation/copy_button_hovered"), CustomMachinery.rl("creation/copy_button_disabled_hovered"));
    public static final WidgetSprites PASTE_SPRITES = new WidgetSprites(CustomMachinery.rl("creation/paste_button"), CustomMachinery.rl("creation/paste_button_disabled"), CustomMachinery.rl("creation/paste_button_hovered"), CustomMachinery.rl("creation/paste_button_disabled_hovered"));
    public static final WidgetSprites ALIGN_TOP_SPRITES = new WidgetSprites(CustomMachinery.rl("creation/align_top_button"), CustomMachinery.rl("creation/align_top_button_disabled"), CustomMachinery.rl("creation/align_top_button_hovered"), CustomMachinery.rl("creation/align_top_button_disabled_hovered"));
    public static final WidgetSprites ALIGN_CENTER_SPRITES = new WidgetSprites(CustomMachinery.rl("creation/align_center_button"), CustomMachinery.rl("creation/align_center_button_disabled"), CustomMachinery.rl("creation/align_center_button_hovered"), CustomMachinery.rl("creation/align_center_button_disabled_hovered"));
    public static final WidgetSprites ALIGN_BOTTOM_SPRITES = new WidgetSprites(CustomMachinery.rl("creation/align_bottom_button"), CustomMachinery.rl("creation/align_bottom_button_disabled"), CustomMachinery.rl("creation/align_bottom_button_hovered"), CustomMachinery.rl("creation/align_bottom_button_disabled_hovered"));
    public static final WidgetSprites ALIGN_LEFT_SPRITES = new WidgetSprites(CustomMachinery.rl("creation/align_left_button"), CustomMachinery.rl("creation/align_left_button_disabled"), CustomMachinery.rl("creation/align_left_button_hovered"), CustomMachinery.rl("creation/align_left_button_disabled_hovered"));
    public static final WidgetSprites ALIGN_MIDDLE_SPRITES = new WidgetSprites(CustomMachinery.rl("creation/align_middle_button"), CustomMachinery.rl("creation/align_middle_button_disabled"), CustomMachinery.rl("creation/align_middle_button_hovered"), CustomMachinery.rl("creation/align_middle_button_disabled_hovered"));
    public static final WidgetSprites ALIGN_RIGHT_SPRITES = new WidgetSprites(CustomMachinery.rl("creation/align_right_button"), CustomMachinery.rl("creation/align_right_button_disabled"), CustomMachinery.rl("creation/align_right_button_hovered"), CustomMachinery.rl("creation/align_right_button_disabled_hovered"));

    private final GuiEditorWidget guiEditor;
    public ImageButton revertButton;
    public ImageButton copyButton;
    public ImageButton pasteButton;
    public ImageButton alignTop;
    public ImageButton alignCenter;
    public ImageButton alignBottom;
    public ImageButton alignLeft;
    public ImageButton alignMiddle;
    public ImageButton alignRight;

    public GuiTab(MachineEditScreen parent) {
        super(Component.translatable("custommachinery.gui.creation.tab.gui"), parent);
        RowHelper row = this.layout.createRowHelper(1);
        row.defaultCellSetting().paddingTop(2);
        row.addChild(new StringWidget(parent.width, 0, Component.empty(), Minecraft.getInstance().font));
        BackgroundGuiElement background = parent.getBuilder().getGuiElements().stream().filter(element -> element instanceof BackgroundGuiElement).map(element -> (BackgroundGuiElement)element).findFirst().orElse(null);
        if(background != null)
            this.guiEditor = row.addChild(new GuiEditorWidget(parent, parent.x, parent.y, background.getWidth(), background.getHeight(), parent.getBuilder().getGuiElements()), row.newCellSettings().alignHorizontallyCenter());
        else
            this.guiEditor = row.addChild(new GuiEditorWidget(parent, parent.x, parent.y, 256, 192, parent.getBuilder().getGuiElements()), row.newCellSettings().alignHorizontallyCenter());
    }

    public void setSize(int width, int height) {
        this.guiEditor.setSize(width, height);
        this.layout.arrangeElements();
    }

    @Override
    public List<AbstractWidget> getToolButtons() {
        ImageButton addButton = new ImageButton(0, 0, 20, 20, CREATE_SPRITES, button -> this.create());
        addButton.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.add")));

        ImageButton backgroundButton = new ImageButton(0, 0, 20, 20, BACKGROUND_SPRITES, button -> this.background());
        backgroundButton.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.background")));

        ImageButton gridButton = new ImageButton(0, 0, 20, 20, GRID_SPRITES, button -> this.grid());
        gridButton.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.grid")));

        this.revertButton = new ImageButton(0, 0, 20, 20, REVERT_SPRITES, button -> this.guiEditor.revertChange());
        this.revertButton.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.revert.tooltip").append("\n").append(Component.translatable("custommachinery.gui.creation.gui.revert.tooltip2").withStyle(ChatFormatting.GRAY))));
        this.revertButton.active = false;

        this.copyButton = new ImageButton(0, 0, 20, 20, COPY_SPRITES, button -> this.guiEditor.copy());
        this.copyButton.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.copy.tooltip").append("\n").append(Component.translatable("custommachinery.gui.creation.gui.copy.tooltip2").withStyle(ChatFormatting.GRAY))));
        this.copyButton.active = false;

        this.pasteButton = new ImageButton(0, 0, 20, 20, PASTE_SPRITES, button -> this.guiEditor.paste());
        this.pasteButton.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.paste.tooltip").append("\n").append(Component.translatable("custommachinery.gui.creation.gui.paste.tooltip2").withStyle(ChatFormatting.GRAY))));
        this.pasteButton.active = false;

        this.alignTop = new ImageButton(0, 0, 20, 20, ALIGN_TOP_SPRITES, button -> this.guiEditor.alignTop());
        this.alignTop.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.align.top")));

        this.alignCenter = new ImageButton(0, 0, 20, 20, ALIGN_CENTER_SPRITES, button -> this.guiEditor.alignCenter());
        this.alignCenter.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.align.center")));

        this.alignBottom = new ImageButton(0, 0, 20, 20, ALIGN_BOTTOM_SPRITES, button -> this.guiEditor.alignBottom());
        this.alignBottom.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.align.bottom")));

        this.alignLeft = new ImageButton(0, 0, 20, 20, ALIGN_LEFT_SPRITES, button -> this.guiEditor.alignLeft());
        this.alignLeft.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.align.left")));

        this.alignMiddle = new ImageButton(0, 0, 20, 20, ALIGN_MIDDLE_SPRITES, button -> this.guiEditor.alignMiddle());
        this.alignMiddle.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.align.middle")));

        this.alignRight = new ImageButton(0, 0, 20, 20, ALIGN_RIGHT_SPRITES, button -> this.guiEditor.alignRight());
        this.alignRight.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.align.right")));

        this.enableAlignButtons(false);

        ImageWidget empty = ImageWidget.texture(20, 20, CustomMachinery.rl("textures/gui/base_empty.png"), 1, 1);

        //Order from top left to bottom right, max of 3 columns and 5 rows
        return List.of(
                this.alignTop,      this.alignLeft,    addButton,
                this.alignCenter,   this.alignMiddle,  backgroundButton,
                this.alignBottom,   this.alignRight,   gridButton,
                this.copyButton,    this.pasteButton,  this.revertButton);
    }

    private void create() {
        this.parent.openPopup(new GuiElementCreationPopup(this.parent, this.guiEditor::addCreatedElement));
    }

    private void background() {
        this.parent.openPopup(new BackgroundEditorPopup(this.parent, this.guiEditor), "background");
    }

    private void grid() {
        this.parent.openPopup(new GridEditorPopup(this.parent, this.guiEditor), "grid");
    }

    public void enableAlignButtons(boolean active) {
        this.alignTop.active = active;
        this.alignCenter.active = active;
        this.alignBottom.active = active;
        this.alignLeft.active = active;
        this.alignMiddle.active = active;
        this.alignRight.active = active;
    }
}

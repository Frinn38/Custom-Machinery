package fr.frinn.custommachinery.client.screen.creation.tabs;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.gui.BackgroundEditorPopup;
import fr.frinn.custommachinery.client.screen.creation.gui.GridEditorPopup;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiEditorWidget;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiElementCreationPopup;
import fr.frinn.custommachinery.common.guielement.BackgroundGuiElement;
import fr.frinn.custommachinery.common.util.CycleTimer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
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
    public static final WidgetSprites CENTER_HORIZONTALLY_SPRITES = new WidgetSprites(CustomMachinery.rl("creation/center_horizontally_button"), CustomMachinery.rl("creation/center_horizontally_button_disabled"), CustomMachinery.rl("creation/center_horizontally_button_hovered"), CustomMachinery.rl("creation/center_horizontally_button_disabled_hovered"));
    public static final WidgetSprites CENTER_VERTICALLY_SPRITES = new WidgetSprites(CustomMachinery.rl("creation/center_vertically_button"), CustomMachinery.rl("creation/center_vertically_button_disabled"), CustomMachinery.rl("creation/center_vertically_button_hovered"), CustomMachinery.rl("creation/center_vertically_button_disabled_hovered"));
    public static final WidgetSprites COMPACT_SPRITES = new WidgetSprites(CustomMachinery.rl("creation/compact_button"), CustomMachinery.rl("creation/compact_button_disabled"), CustomMachinery.rl("creation/compact_button_hovered"), CustomMachinery.rl("creation/compact_button_disabled_hovered"));

    private final GuiEditorWidget guiEditor;
    private final StringWidget empty;
    public ImageButton revert;
    public ImageButton copy;
    public ImageButton paste;
    public ImageButton alignTop;
    public ImageButton alignCenter;
    public ImageButton alignBottom;
    public ImageButton alignLeft;
    public ImageButton alignMiddle;
    public ImageButton alignRight;
    public ImageButton centerHorizontally;
    public ImageButton centerVertically;
    public ImageButton compact;

    public GuiTab(MachineEditScreen parent) {
        super(Component.translatable("custommachinery.gui.creation.tab.gui"), parent);
        RowHelper row = this.layout.createRowHelper(2);
        row.defaultCellSetting().paddingTop(2);
        BackgroundGuiElement background = parent.getBuilder().getGuiElements().stream().filter(element -> element instanceof BackgroundGuiElement).map(element -> (BackgroundGuiElement)element).findFirst().orElse(null);
        if(background != null)
            this.guiEditor = row.addChild(new GuiEditorWidget(parent, parent.x, parent.y, background.getWidth(), background.getHeight(), parent.getBuilder().getGuiElements()), 2, row.newCellSettings().alignHorizontallyCenter());
        else
            this.guiEditor = row.addChild(new GuiEditorWidget(parent, parent.x, parent.y, 256, 192, parent.getBuilder().getGuiElements()), 2, row.newCellSettings().alignHorizontallyCenter());
        this.empty = row.addChild(new StringWidget(this.guiEditor.getWidth(), 192 - this.guiEditor.getHeight(), Component.empty(), Minecraft.getInstance().font), 2);
        HintWidget hintWidget = row.addChild(new HintWidget(180), row.newCellSettings().alignHorizontallyLeft());
        hintWidget.alignLeft();
        MousePosWidget mousePosWidget = row.addChild(new MousePosWidget(48), row.newCellSettings().alignHorizontallyRight());
        mousePosWidget.alignRight();
    }

    public void setSize(int width, int height) {
        this.guiEditor.setSize(width, height);
        this.empty.setSize(width, 192 - height);
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

        this.revert = new ImageButton(0, 0, 20, 20, REVERT_SPRITES, button -> this.guiEditor.revertChange());
        this.revert.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.revert").append("\n").append(Component.translatable("custommachinery.gui.creation.gui.revert.key").withStyle(ChatFormatting.GRAY))));
        this.revert.active = false;

        this.copy = new ImageButton(0, 0, 20, 20, COPY_SPRITES, button -> this.guiEditor.copy());
        this.copy.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.copy").append("\n").append(Component.translatable("custommachinery.gui.creation.gui.copy.key").withStyle(ChatFormatting.GRAY))));
        this.copy.active = false;

        this.paste = new ImageButton(0, 0, 20, 20, PASTE_SPRITES, button -> this.guiEditor.paste());
        this.paste.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.paste").append("\n").append(Component.translatable("custommachinery.gui.creation.gui.paste.key").withStyle(ChatFormatting.GRAY))));
        this.paste.active = false;

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

        this.centerHorizontally = new ImageButton(0, 0, 20, 20, CENTER_HORIZONTALLY_SPRITES, button -> this.guiEditor.centerHorizontally());
        this.centerHorizontally.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.center.horizontally")));
        this.centerHorizontally.active = false;

        this.centerVertically = new ImageButton(0, 0, 20, 20, CENTER_VERTICALLY_SPRITES, button -> this.guiEditor.centerVertically());
        this.centerVertically.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.center.vertically")));
        this.centerVertically.active = false;

        this.compact = new ImageButton(0, 0, 20, 20, COMPACT_SPRITES, button -> this.guiEditor.compact());
        this.compact.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.compact")));
        this.compact.active = false;

        this.enableAlignButtons(false);

        ImageWidget empty = ImageWidget.texture(20, 20, CustomMachinery.rl("textures/gui/base_empty.png"), 1, 1);

        //Order from top left to bottom right, max of 3 columns and 5 rows
        return List.of(
                this.alignTop,           this.alignLeft,        addButton,
                this.alignCenter,        this.alignMiddle,      backgroundButton,
                this.alignBottom,        this.alignRight,       gridButton,
                this.centerHorizontally, this.centerVertically, this.compact,
                this.copy,               this.paste,            this.revert
        );
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
        this.compact.active = active;
    }

    private static class HintWidget extends StringWidget {

        private final List<Component> hints = new ArrayList<>();
        private final CycleTimer hintsTimer = new CycleTimer(() -> 10000);

        public HintWidget(int width) {
            super(width, Minecraft.getInstance().font.lineHeight, Component.empty(), Minecraft.getInstance().font);
            for(int i = 1; i < 4; i++)
                this.hints.add(Component.translatable("custommachinery.gui.creation.gui.hints", Component.translatable("custommachinery.gui.creation.gui.hints." + i)).withStyle(ChatFormatting.DARK_GRAY));
        }

        @Override
        public Component getMessage() {
            return this.hintsTimer.getOrDefault(this.hints, Component.empty());
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            this.hintsTimer.onDraw();
            Font font = Minecraft.getInstance().font;
            Component hint = this.getMessage();
            float scale = Math.clamp(this.getWidth() / (float)(font.width(hint) - 50), 0.5F, 1.0F);
            graphics.pose().pushPose();
            graphics.pose().scale(scale, scale, 1F);
            graphics.drawString(Minecraft.getInstance().font, this.getMessage(), (int)(this.getX() / scale), (int)((this.getY() + 1) / scale), 0, false);
            graphics.pose().popPose();
        }
    }

    private class MousePosWidget extends StringWidget {

        public MousePosWidget(int width) {
            super(width, Minecraft.getInstance().font.lineHeight, Component.empty(), Minecraft.getInstance().font);
        }

        @Override
        public Component getMessage() {
            return Component.translatable("custommachinery.gui.creation.gui.mouse", Math.clamp((int)(Minecraft.getInstance().mouseHandler.xpos() / Minecraft.getInstance().getWindow().getGuiScale() - GuiTab.this.guiEditor.getX()), 0, GuiTab.this.guiEditor.getWidth()), Math.clamp((int)(Minecraft.getInstance().mouseHandler.ypos() / Minecraft.getInstance().getWindow().getGuiScale() - GuiTab.this.guiEditor.getY()), 0, GuiTab.this.guiEditor.getHeight()));
        }
    }
}

package fr.frinn.custommachinery.client.screen.creation.tabs;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.gui.BackgroundEditorPopup;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiEditorWidget;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiElementCreationPopup;
import fr.frinn.custommachinery.common.guielement.BackgroundGuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

public class GuiTab extends MachineEditTab {

    public static final WidgetSprites CREATE_SPRITES = new WidgetSprites(CustomMachinery.rl("creation/create_button"), CustomMachinery.rl("creation/create_button_hovered"));
    public static final WidgetSprites BACKGROUND_SPRITES = new WidgetSprites(CustomMachinery.rl("creation/background_button"), CustomMachinery.rl("creation/background_button_hovered"));

    private final GuiEditorWidget guiEditor;
    private AddGuiElementButton addButton;
    private ImageButton backgroundButton;

    public GuiTab(MachineEditScreen parent) {
        super(Component.translatable("custommachinery.gui.creation.tab.gui"), parent);
        RowHelper row = this.layout.createRowHelper(1);
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
    public void opened() {
        this.addButton = this.parent.addRenderableWidget(new AddGuiElementButton(this.parent.x - 28, this.parent.y + 85, button -> this.create()));
        this.backgroundButton = this.parent.addRenderableWidget(new ImageButton(this.parent.x - 28, this.parent.y + 110, 20, 20, BACKGROUND_SPRITES, button -> this.background()));
        this.backgroundButton.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.background")));
    }

    @Override
    public void closed() {
        this.parent.removeWidget(this.addButton);
        this.parent.removeWidget(this.backgroundButton);
    }

    @Override
    public void doLayout(ScreenRectangle rectangle) {
        super.doLayout(rectangle);
        if(this.addButton != null)
            this.addButton.setPosition(this.parent.x - 28, this.parent.y + 85);
        if(this.backgroundButton != null)
            this.backgroundButton.setPosition(this.parent.x - 28, this.parent.y + 110);
    }

    private void create() {
        this.parent.openPopup(new GuiElementCreationPopup(this.parent, this.guiEditor::addCreatedElement));
    }

    private void background() {
        this.parent.openPopup(new BackgroundEditorPopup(this.parent));
    }

    public static class AddGuiElementButton extends ImageButton {

        public AddGuiElementButton(int x, int y, OnPress onPress) {
            super(x, y, 20, 20, CREATE_SPRITES, onPress);
            this.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.add")));
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            BaseScreen.blankBackground(graphics, this.getX() - 5, this.getY() - 5, this.getWidth() + 10, this.getHeight() + 35);
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
        }
    }
}

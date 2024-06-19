package fr.frinn.custommachinery.client.screen.creation.tabs;

import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiEditorWidget;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiElementCreationPopup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.network.chat.Component;

public class GuiTab extends MachineEditTab {

    private final GuiEditorWidget guiEditor;
    private AddGuiElementButton addButton;

    public GuiTab(MachineEditScreen parent) {
        super(Component.translatable("custommachinery.gui.creation.tab.gui"), parent);
        RowHelper row = this.layout.createRowHelper(1);
        row.addChild(new StringWidget(parent.width, 0, Component.empty(), Minecraft.getInstance().font));
        this.guiEditor = row.addChild(new GuiEditorWidget(parent, parent.x, parent.y, 256, 192, parent.getBuilder().getGuiElements()), row.newCellSettings().alignHorizontallyCenter());
    }

    @Override
    public void opened() {
        this.addButton = this.parent.addRenderableWidget(new AddGuiElementButton(this.parent.x - 28, this.parent.y + 60, button -> this.create()));
    }

    @Override
    public void closed() {
        this.parent.removeWidget(this.addButton);
    }

    private void create() {
        this.parent.openPopup(new GuiElementCreationPopup(this.parent, this.guiEditor::addCreatedElement));
    }

    public static class AddGuiElementButton extends ImageButton {

        public AddGuiElementButton(int x, int y, OnPress onPress) {
            super(x, y, 20, 20, 40, 0, MachineEditScreen.WIDGETS, onPress);
            this.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.add")));
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            BaseScreen.blankBackground(graphics, this.getX() - 5, this.getY() - 5, this.getWidth() + 10, this.getHeight() + 10);
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
        }
    }
}

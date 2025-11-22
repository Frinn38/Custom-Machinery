package fr.frinn.custommachinery.client.screen.creation.upgrade.tabs;

import fr.frinn.custommachinery.client.screen.creation.upgrade.UpgradeEditScreen;
import fr.frinn.custommachinery.client.screen.widget.IntegerSlider;
import fr.frinn.custommachinery.client.screen.widget.ItemSelectionButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.components.toasts.TutorialToast.Icons;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;

public class UpgradeBaseInfoTab extends UpgradeEditTab {

    public UpgradeBaseInfoTab(UpgradeEditScreen parent) {
        super(Component.translatable("custommachinery.gui.creation.tab.base_info"), parent);
        final Font font = this.parent.mc.font;

        //Each row must be the same amount of columns as defined here
        RowHelper row = this.layout.rowSpacing(8).createRowHelper(2);
        row.defaultCellSetting().paddingHorizontal(0);
        LayoutSettings middle = row.newCellSettings().alignVerticallyMiddle();
        LayoutSettings left = row.newCellSettings().alignHorizontallyLeft();

        //Id (1rst row)
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.upgrade.base_info.id"), font), middle);
        row.addChild(new UpgradeIdWidget(150, 9, Component.literal(this.parent.getLocation().id().toString()), font), left);

        //Item (2nd row)
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.upgrade.base_info.item"), font), middle);
        ItemSelectionButton itemButton = row.addChild(new ItemSelectionButton(parent, 0, 0, 40, 40), left);
        itemButton.setItem(parent.getBuilder().getItem());
        itemButton.setResponder(item -> {
            parent.getBuilder().setItem(item);
            parent.setChanged();
        });

        //Max (3rd row)
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.upgrade.base_info.max"), font), middle);
        row.addChild(IntegerSlider.builder().displayOnlyValue().bounds(1, 64).defaultValue(parent.getBuilder().getMax()).setResponder(max -> {
            parent.getBuilder().setMax(max);
            parent.setChanged();
        }).create(0, 0, 100, 20, Component.empty()), left).setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.upgrade.base_info.max.tooltip")));
    }

    private static class UpgradeIdWidget extends StringWidget {

        public UpgradeIdWidget(int width, int height, Component message, Font font) {
            super(width, height, message, font);
            this.active = true;
            this.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.base_info.id.tooltip")));
        }

        @Override
        public void onClick(double mouseX, double mouseY, int button) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getMessage().getString());
            Minecraft.getInstance().getTutorial().addTimedToast(new TutorialToast(Icons.MOUSE, Component.translatable("custommachinery.gui.creation.base_info.id.copied"), null, false), 50);
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.drawScrollingString(this.getFont(), this.getMessage(), this.getX(), this.getX() + this.getWidth(), this.getY(), this.getColor());
        }
    }
}

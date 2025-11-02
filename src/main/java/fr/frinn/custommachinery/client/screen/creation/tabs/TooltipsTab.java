package fr.frinn.custommachinery.client.screen.creation.tabs;

import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.common.guielement.SlotGuiElement;
import fr.frinn.custommachinery.common.init.CustomMachineItem;
import fr.frinn.custommachinery.impl.util.TextComponentUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class TooltipsTab extends MachineEditTab {

    public TooltipsTab(MachineEditScreen parent) {
        super(Component.translatable("custommachinery.gui.creation.tab.tooltips"), parent);
        RowHelper row = this.layout.createRowHelper(1);
        row.defaultCellSetting().paddingTop(5);
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.tooltips.title"), Minecraft.getInstance().font));
        MultiLineEditBox box = row.addChild(new MultiLineEditBox(Minecraft.getInstance().font, 0, 0, 200, 100, Component.empty(), Component.empty()));
        box.setValue(this.componentListToString(parent.getBuilder().getTooltips()));
        box.setValueListener(value -> {
            parent.getBuilder().setTooltips(this.stringToComponentList(value));
            parent.setChanged();
        });
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.tooltips.result"), Minecraft.getInstance().font));
        row.addChild(new ItemSlotWidget());
    }

    private String componentListToString(List<Component> tooltips) {
        StringBuilder builder = new StringBuilder();
        Iterator<Component> iterator = tooltips.iterator();
        while (iterator.hasNext()) {
            builder.append(TextComponentUtils.getString(iterator.next()));
            if(iterator.hasNext())
                builder.append("\n");
        }
        return builder.toString();
    }

    private List<Component> stringToComponentList(String s) {
        return Arrays.stream(s.split("\n")).filter(string -> !string.isEmpty()).<Component>map(Component::translatable).toList();
    }

    private class ItemSlotWidget extends AbstractWidget {

        public ItemSlotWidget() {
            super(0, 0, 18, 18, TooltipsTab.this.parent.getBuilder().getName());
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            ClientHandler.blit(graphics, SlotGuiElement.BASE_TEXTURE, this.getX(), this.getY(), this.width, this.height);
            graphics.renderFakeItem(CustomMachineItem.makeMachineItem(TooltipsTab.this.parent.getBuilder().getLocation().id()), this.getX() + 1, this.getY() + 1);
            if(mouseX >= this.getX() + 1 && mouseX <= this.getX() + 16 && mouseY >= this.getY() + 1 && mouseY <= this.getY() + 16) {
                ClientHandler.renderSlotHighlight(graphics, this.getX() + 1, this.getY() + 1, 16, 16);
                graphics.renderComponentTooltip(Minecraft.getInstance().font, this.getTooltips(), mouseX, mouseY);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }

        private List<Component> getTooltips() {
            return CustomMachineItem.makeMachineItem(TooltipsTab.this.parent.getBuilder().getLocation().id()).getTooltipLines(TooltipContext.of(Minecraft.getInstance().level), Minecraft.getInstance().player, new EditorTooltipFlag(TooltipsTab.this.parent.getBuilder().getTooltips()));
        }
    }

    public record EditorTooltipFlag(List<Component> tooltips) implements TooltipFlag {

        @Override
        public boolean isAdvanced() {
            return true;
        }

        @Override
        public boolean isCreative() {
            return true;
        }
    }
}

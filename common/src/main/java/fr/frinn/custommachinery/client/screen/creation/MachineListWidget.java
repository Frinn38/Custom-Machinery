package fr.frinn.custommachinery.client.screen.creation;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineListWidget.MachineEntry;
import fr.frinn.custommachinery.common.init.CustomMachineItem;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;

public class MachineListWidget extends ObjectSelectionList<MachineEntry> {

    private final MachineCreationScreen parent;

    public MachineListWidget(MachineCreationScreen parent, Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight) {
        super(minecraft, width, height, y0, y1, itemHeight);
        this.parent = parent;
        this.setRenderBackground(false);
        this.setRenderHeader(false, 0);
        this.setRenderTopAndBottom(false);
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getRowRight();
    }

    public void reload() {
        this.children().clear();
        CustomMachinery.MACHINES.values().forEach(machine -> this.addEntry(new MachineEntry(machine)));
    }

    @Override
    protected void renderItem(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, int index, int left, int top, int width, int height) {
        MachineEntry entry = this.getEntry(index);
        entry.renderBack(graphics, index, top, left, width, height, mouseX, mouseY, entry.isMouseOver(mouseX, mouseY), partialTick);
        if(this.isSelectedItem(index))
            this.renderSelection(graphics, top, width, height, FastColor.ARGB32.color(255, 0, 0, 0), FastColor.ARGB32.color(255, 198, 198, 198));
        entry.render(graphics, index, top, left, width, height, mouseX, mouseY, entry.isMouseOver(mouseX, mouseY), partialTick);
    }

    public static class MachineEntry extends Entry<MachineEntry> {

        private final Minecraft mc = Minecraft.getInstance();
        private final CustomMachine machine;

        public MachineEntry(CustomMachine machine) {
            this.machine = machine;
        }

        public CustomMachine getMachine() {
            return this.machine;
        }

        @Override
        public Component getNarration() {
            return this.machine.getName();
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            graphics.renderItem(CustomMachineItem.makeMachineItem(this.machine.getId()), left + 2, top + height / 2 - 8);
            graphics.drawString(this.mc.font, this.machine.getName(), left + 20, top + height / 2 - this.mc.font.lineHeight / 2 - 6, 0, false);
            BaseScreen.drawScaledString(graphics, this.mc.font, Component.literal(this.machine.getId().toString()).withStyle(ChatFormatting.DARK_GRAY), left + 20, top + height / 2 - this.mc.font.lineHeight / 2 + 2, 0.8f, 0, false);
            BaseScreen.drawScaledString(graphics, this.mc.font, this.machine.getLocation().getLoader().getTranslatedName().withStyle(ChatFormatting.ITALIC), left + 20, top + height / 2 - this.mc.font.lineHeight / 2 + 9, 0.7f, 0, false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return true;
        }
    }
}

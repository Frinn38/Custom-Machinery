package fr.frinn.custommachinery.client.screen.creation;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.popup.ConfirmPopup;
import fr.frinn.custommachinery.common.init.CustomMachineItem;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.network.CRemoveMachinePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class DeleteMachinePopup extends ConfirmPopup {

    private final CustomMachine machine;

    public DeleteMachinePopup(BaseScreen parent, CustomMachine machine) {
        super(parent, 128, 96, () -> {});
        this.machine = machine;
        this.title(Component.translatable("custommachinery.gui.popup.warning").withStyle(ChatFormatting.DARK_RED));
        this.text(Component.translatable("custommachinery.gui.creation.delete.popup"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        int itemX = this.x + this.xSize / 2 - 8;
        int itemY = this.y + this.ySize / 2 - 5;
        graphics.renderItem(CustomMachineItem.makeMachineItem(this.machine.getId()), itemX, itemY);
        if(this.isMouseOver(mouseX, mouseY) && mouseX >= itemX && mouseX <= itemX + 16 && mouseY >= itemY && mouseY <= itemY + 16)
            graphics.renderTooltip(Minecraft.getInstance().font, this.machine.getName(), mouseX, mouseY);
    }

    @Override
    public void confirm() {
        new CRemoveMachinePacket(this.machine.getId()).sendToServer();
        CustomMachinery.MACHINES.remove(this.machine.getId());
        if(this.parent instanceof MachineCreationScreen creationScreen)
            creationScreen.reloadList();
        super.confirm();
    }
}

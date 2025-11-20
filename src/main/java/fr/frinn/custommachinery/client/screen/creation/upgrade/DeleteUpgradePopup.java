package fr.frinn.custommachinery.client.screen.creation.upgrade;

import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.popup.ConfirmPopup;
import fr.frinn.custommachinery.common.network.CRemoveUpgradePacket;
import fr.frinn.custommachinery.common.upgrade.MachineUpgrade;
import fr.frinn.custommachinery.common.upgrade.UpgradeLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class DeleteUpgradePopup extends ConfirmPopup {

    private final UpgradeLocation location;
    private final MachineUpgrade upgrade;

    public DeleteUpgradePopup(BaseScreen parent, UpgradeLocation location, MachineUpgrade upgrade) {
        super(parent, 128, 96, () -> {
        });
        this.location = location;
        this.upgrade = upgrade;
        this.title(Component.translatable("custommachinery.gui.popup.warning").withStyle(ChatFormatting.DARK_RED));
        this.text(Component.translatable("custommachinery.gui.creation.upgrade.popup.delete"), Component.empty(), Component.empty());//2 empty lines for machine display
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        int itemX = this.x + this.xSize / 2 - 8;
        int itemY = this.y + this.ySize / 2 + 2;
        ItemStack stack = this.upgrade.item().getDefaultInstance();
        graphics.renderItem(stack, itemX, itemY);
        if (this.isMouseOver(mouseX, mouseY) && mouseX >= itemX && mouseX <= itemX + 16 && mouseY >= itemY && mouseY <= itemY + 16)
            graphics.renderTooltip(Minecraft.getInstance().font, stack, mouseX, mouseY);
    }

    @Override
    public void confirm() {
        PacketDistributor.sendToServer(new CRemoveUpgradePacket(this.location.id()));
        super.confirm();
    }
}
package fr.frinn.custommachinery.client.screen.creation;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.popup.InfoPopup;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.common.network.CAddMachinePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class CreateMachinePopup extends PopupScreen {

    private Button create;
    private Button delete;
    private EditBox id;
    private EditBox name;

    protected CreateMachinePopup(BaseScreen parent) {
        super(parent, 128, 96);
    }

    public void create() {
        new CAddMachinePacket(this.id.getValue(), Component.literal(this.name.getValue())).sendToServer();
        this.parent.closePopup(this);
        this.parent.openPopup(new InfoPopup(this.parent, 144, 96).text(Component.translatable("custommachinery.gui.creation.popup.create.success")));
    }

    @Override
    protected void init() {
        super.init();
        this.create = this.addRenderableWidget(Button.builder(Component.translatable("custommachinery.gui.creation.create").withStyle(ChatFormatting.GREEN), button -> this.create()).bounds(this.x + 10, this.y + this.ySize - 30, 50, 20).build());
        this.delete = this.addRenderableWidget(Button.builder(Component.translatable("custommachinery.gui.popup.cancel").withStyle(ChatFormatting.DARK_RED), button -> this.parent.closePopup(this)).bounds(this.x + this.xSize - 60, this.y + this.ySize - 30, 50, 20).build());
        this.id = this.addRenderableWidget(new EditBox(this.font, this.x + 10, this.y + 20, this.xSize - 20, 20, Component.literal("machine_id")));
        this.id.setFilter(s -> {
            for(char c : s.toCharArray())
                if(!ResourceLocation.validPathChar(c))
                    return false;
            return true;
        });
        this.id.setHint(Component.literal("machine_id"));
        this.id.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.popup.create.id.tooltip")));
        this.name = this.addRenderableWidget(new EditBox(this.font, this.x + 10, this.y + 43, this.xSize - 20, 20, Component.literal("Machine name")));
        this.name.setHint(Component.literal("Machine name"));
        this.name.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.popup.create.name.tooltip")));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.create.active = !(this.id.getValue().isEmpty() && ResourceLocation.isValidResourceLocation(CustomMachinery.MODID + ":" + this.id.getValue()) && this.name.getValue().isEmpty());
        drawCenteredString(graphics, Minecraft.getInstance().font, Component.translatable("custommachinery.gui.creation.popup.create"), this.x + this.xSize / 2, this.y + 10, 0, false);
    }
}

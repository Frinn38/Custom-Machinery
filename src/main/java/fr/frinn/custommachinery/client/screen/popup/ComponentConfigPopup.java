package fr.frinn.custommachinery.client.screen.popup;

import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.client.screen.MachineConfigScreen;
import fr.frinn.custommachinery.client.screen.widget.config.AutoIOModeButton;
import fr.frinn.custommachinery.client.screen.widget.config.SideModeButton;
import fr.frinn.custommachinery.common.network.CAllSidesNonePacket;
import fr.frinn.custommachinery.common.network.CChangeSideModePacket;
import fr.frinn.custommachinery.impl.component.config.IOSideConfig;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import fr.frinn.custommachinery.impl.component.config.SideConfig.ConfigGuiData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class ComponentConfigPopup extends PopupScreen {

    private final SideConfig<?> config;
    private final ConfigGuiData guiData;

    public ComponentConfigPopup(MachineConfigScreen parent, SideConfig<?> config) {
        super(parent, config.getGuiData().width(), config.getGuiData().height());
        this.config = config;
        this.guiData = config.getGuiData();
    }

    @Override
    protected void init() {
        super.init();
        //TOP
        this.addRenderableWidget(new SideModeButton(this.x, this.y, () -> this.config.getSideMode(RelativeSide.TOP), RelativeSide.TOP, button -> this.setSide(RelativeSide.TOP.ordinal(), true), button -> this.setSide(RelativeSide.TOP.ordinal(), false), this.guiData.top()));
        //LEFT
        this.addRenderableWidget(new SideModeButton(this.x, this.y, () -> this.config.getSideMode(RelativeSide.LEFT), RelativeSide.LEFT, button -> this.setSide(RelativeSide.LEFT.ordinal(), true), button -> this.setSide(RelativeSide.LEFT.ordinal(), false), this.guiData.left()));
        //FRONT
        this.addRenderableWidget(new SideModeButton(this.x, this.y, () -> this.config.getSideMode(RelativeSide.FRONT), RelativeSide.FRONT, button -> this.setSide(RelativeSide.FRONT.ordinal(), true), button -> this.setSide(RelativeSide.FRONT.ordinal(), false), this.guiData.front()));
        //RIGHT
        this.addRenderableWidget(new SideModeButton(this.x, this.y, () -> this.config.getSideMode(RelativeSide.RIGHT), RelativeSide.RIGHT, button -> this.setSide(RelativeSide.RIGHT.ordinal(), true), button -> this.setSide(RelativeSide.RIGHT.ordinal(), false), this.guiData.right()));
        //BACK
        this.addRenderableWidget(new SideModeButton(this.x, this.y, () -> this.config.getSideMode(RelativeSide.BACK), RelativeSide.BACK, button -> this.setSide(RelativeSide.BACK.ordinal(), true), button -> this.setSide(RelativeSide.BACK.ordinal(), false), this.guiData.back()));
        //BOTTOM
        this.addRenderableWidget(new SideModeButton(this.x, this.y, () -> this.config.getSideMode(RelativeSide.BOTTOM), RelativeSide.BOTTOM, button -> this.setSide(RelativeSide.BOTTOM.ordinal(), true), button -> this.setSide(RelativeSide.BOTTOM.ordinal(), false), this.guiData.bottom()));
        if(this.config instanceof IOSideConfig ioSideConfig) {
            //AUTO-INPUT
            this.addRenderableWidget(new AutoIOModeButton(this.x, this.y, ioSideConfig::isAutoInput, true, button -> this.setSide(6, true), this.guiData.input()));
            //AUTO-OUTPUT
            this.addRenderableWidget(new AutoIOModeButton(this.x, this.y, ioSideConfig::isAutoOutput, false, button -> this.setSide(7, true), this.guiData.output()));
        }
        //All sides none
        ImageButton allNone = this.addRenderableWidget(new ImageButton(this.x + this.guiData.none().x(), this.y + this.guiData.none().y(), this.guiData.none().width(), this.guiData.none().height(), ClientHandler.dataToSprite(this.guiData.none().sprites()), button -> this.setAllNone()));
        allNone.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.config.all_none")));
        //EXIT
        ImageButton close = this.addRenderableWidget(new ImageButton(this.x + this.guiData.exit().x(), this.y + this.guiData.exit().y(), this.guiData.exit().width(), this.guiData.exit().height(), ClientHandler.dataToSprite(this.guiData.exit().sprites()), button -> this.parent.closePopup(this)));
        close.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.config.close")));
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.blit(this.guiData.background().texture(), this.x, this.y, this.guiData.background().u(), this.guiData.background().v(), this.xSize, this.ySize, this.xSize, this.ySize);
        graphics.drawString(Minecraft.getInstance().font, this.guiData.title(), (int)(this.x + this.xSize / 2F - this.font.width(this.guiData.title()) / 2F), this.y + 5, 0, false);
    }

    private void setSide(int side, boolean next) {
        if(Minecraft.getInstance().player == null)
            return;
        PacketDistributor.sendToServer(new CChangeSideModePacket(Minecraft.getInstance().player.containerMenu.containerId, getComponentId(), (byte) side, next));
    }

    private void setAllNone() {
        if(Minecraft.getInstance().player != null)
            PacketDistributor.sendToServer(new CAllSidesNonePacket(Minecraft.getInstance().player.containerMenu.containerId, getComponentId()));
    }

    private String getComponentId() {
        return this.config.getComponent().getType().getId() + ":" + this.config.getComponent().getId();
    }
}

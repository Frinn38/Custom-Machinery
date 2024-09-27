package fr.frinn.custommachinery.client.screen.popup;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.MachineConfigScreen;
import fr.frinn.custommachinery.client.screen.widget.config.AutoIOModeButton;
import fr.frinn.custommachinery.client.screen.widget.config.SideModeButton;
import fr.frinn.custommachinery.common.network.CAllSidesNonePacket;
import fr.frinn.custommachinery.common.network.CChangeSideModePacket;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.IOSideConfig;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class ComponentConfigPopup extends PopupScreen {

    private static final WidgetSprites ALL_NONE_SPRITES = new WidgetSprites(CustomMachinery.rl("config/all_none_button"), CustomMachinery.rl("config/all_none_button_hovered"));
    private static final WidgetSprites EXIT_SPRITES = new WidgetSprites(CustomMachinery.rl("config/exit_button"), CustomMachinery.rl("config/exit_button_hovered"));
    private static final Component TITLE = Component.translatable("custommachinery.gui.config.component");

    private final SideConfig<?> config;

    public ComponentConfigPopup(MachineConfigScreen parent, SideConfig<?> config) {
        super(parent, 96, 96);
        this.config = config;
    }

    @Override
    protected void init() {
        super.init();
        //TOP
        this.addRenderableWidget(new SideModeButton(this.x + 41, this.y + 25, () -> this.config.getSideMode(RelativeSide.TOP), RelativeSide.TOP, button -> this.setSide(RelativeSide.TOP.ordinal(), true), button -> this.setSide(RelativeSide.TOP.ordinal(), false)));
        //LEFT
        this.addRenderableWidget(new SideModeButton(this.x + 25, this.y + 41, () -> this.config.getSideMode(RelativeSide.LEFT), RelativeSide.LEFT, button -> this.setSide(RelativeSide.LEFT.ordinal(), true), button -> this.setSide(RelativeSide.LEFT.ordinal(), false)));
        //FRONT
        this.addRenderableWidget(new SideModeButton(this.x + 41, this.y + 41, () -> this.config.getSideMode(RelativeSide.FRONT), RelativeSide.FRONT, button -> this.setSide(RelativeSide.FRONT.ordinal(), true), button -> this.setSide(RelativeSide.FRONT.ordinal(), false)));
        //RIGHT
        this.addRenderableWidget(new SideModeButton(this.x + 57, this.y + 41, () -> this.config.getSideMode(RelativeSide.RIGHT), RelativeSide.RIGHT, button -> this.setSide(RelativeSide.RIGHT.ordinal(), true), button -> this.setSide(RelativeSide.RIGHT.ordinal(), false)));
        //BACK
        this.addRenderableWidget(new SideModeButton(this.x + 25, this.y + 57, () -> this.config.getSideMode(RelativeSide.BACK), RelativeSide.BACK, button -> this.setSide(RelativeSide.BACK.ordinal(), true), button -> this.setSide(RelativeSide.BACK.ordinal(), false)));
        //BOTTOM
        this.addRenderableWidget(new SideModeButton(this.x + 41, this.y + 57, () -> this.config.getSideMode(RelativeSide.BOTTOM), RelativeSide.BOTTOM, button -> this.setSide(RelativeSide.BOTTOM.ordinal(), true), button -> this.setSide(RelativeSide.BOTTOM.ordinal(), false)));
        if(this.config instanceof IOSideConfig ioSideConfig) {
            //AUTO-INPUT
            this.addRenderableWidget(new AutoIOModeButton(this.x + 18, this.y + 75, ioSideConfig::isAutoInput, true, button -> this.setSide(6, true)));
            //AUTO-OUTPUT
            this.addRenderableWidget(new AutoIOModeButton(this.x + 50, this.y + 75, ioSideConfig::isAutoOutput, false, button -> this.setSide(7, true)));
        }
        //All sides none
        ImageButton allNone = this.addRenderableWidget(new ImageButton(this.x + 78, this.y + 57, 14, 14, ALL_NONE_SPRITES, button -> this.setAllNone()));
        allNone.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.config.all_none")));
        //EXIT
        ImageButton close = this.addRenderableWidget(new ImageButton(this.x + 5, this.y + 5, 9, 9, EXIT_SPRITES, button -> this.parent.closePopup(this)));
        close.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.config.close")));
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderBackground(graphics, mouseX, mouseY, partialTicks);
        graphics.drawString(Minecraft.getInstance().font, TITLE, (int)(this.x + this.xSize / 2F - font.width(TITLE) / 2F), this.y + 5, 0, false);
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
        return this.config.getComponent().getType().getId().toString() + ":" + this.config.getComponent().getId();
    }
}

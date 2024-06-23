package fr.frinn.custommachinery.client.screen.popup;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.MachineConfigScreen;
import fr.frinn.custommachinery.client.screen.widget.config.AutoIOModeButtonWidget;
import fr.frinn.custommachinery.client.screen.widget.config.SideModeButtonWidget;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;

public class ComponentConfigPopup extends PopupScreen {

    private static final WidgetSprites EXIT_SPRITES = new WidgetSprites(CustomMachinery.rl("config/exit_button"), CustomMachinery.rl("config/exit_button_hovered"));
    private static final Component TITLE = Component.translatable("custommachinery.gui.config.component");

    private final SideConfig config;

    public ComponentConfigPopup(MachineConfigScreen parent, SideConfig config) {
        super(parent, 96, 96);
        this.config = config;
    }

    @Override
    protected void init() {
        super.init();
        //TOP
        this.addRenderableWidget(new SideModeButtonWidget(this.x + 41, this.y + 25, this.config, RelativeSide.TOP));
        //LEFT
        this.addRenderableWidget(new SideModeButtonWidget(this.x + 25, this.y + 41, this.config, RelativeSide.LEFT));
        //FRONT
        this.addRenderableWidget(new SideModeButtonWidget(this.x + 41, this.y + 41, this.config, RelativeSide.FRONT));
        //RIGHT
        this.addRenderableWidget(new SideModeButtonWidget(this.x + 57, this.y + 41, this.config, RelativeSide.RIGHT));
        //BACK
        this.addRenderableWidget(new SideModeButtonWidget(this.x + 25, this.y + 57, this.config, RelativeSide.BACK));
        //BOTTOM
        this.addRenderableWidget(new SideModeButtonWidget(this.x + 41, this.y + 57, this.config, RelativeSide.BOTTOM));
        //AUTO-INPUT
        this.addRenderableWidget(new AutoIOModeButtonWidget(this.x + 18, this.y + 75, this.config, true));
        //AUTO-OUTPUT
        this.addRenderableWidget(new AutoIOModeButtonWidget(this.x + 50, this.y + 75, this.config, false));
        //EXIT
        this.addRenderableWidget(new ImageButton(this.x + 5, this.y + 5, 9, 9, EXIT_SPRITES, button -> this.parent.closePopup(this)));
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        blankBackground(graphics, this.x, this.y, this.xSize, this.ySize);
        graphics.drawString(Minecraft.getInstance().font, TITLE, (int)(this.x + this.xSize / 2F - font.width(TITLE) / 2F), this.y + 5, 0, false);
    }
}

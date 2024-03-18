package fr.frinn.custommachinery.client.screen.popup;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.widget.TexturedButton;
import fr.frinn.custommachinery.client.screen.widget.config.AutoIOModeButtonWidget;
import fr.frinn.custommachinery.client.screen.widget.config.SideModeButtonWidget;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ComponentConfigPopup extends PopupScreen {

    private static final ResourceLocation EXIT_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/config/exit_button.png");
    private static final ResourceLocation EXIT_TEXTURE_HOVERED = new ResourceLocation(CustomMachinery.MODID, "textures/gui/config/exit_button_hovered.png");
    private static final Component TITLE = Component.translatable("custommachinery.gui.config.component");

    private final SideConfig config;

    public ComponentConfigPopup(SideConfig config) {
        super(96, 96);
        this.config = config;
    }

    @Override
    protected void init() {
        super.init();
        baseMoveDraggingArea();
        //TOP
        this.addRenderableWidget(new SideModeButtonWidget(this.getX() + 41, this.getY() + 25, this.config, RelativeSide.TOP));
        //LEFT
        this.addRenderableWidget(new SideModeButtonWidget(this.getX() + 25, this.getY() + 41, this.config, RelativeSide.LEFT));
        //FRONT
        this.addRenderableWidget(new SideModeButtonWidget(this.getX() + 41, this.getY() + 41, this.config, RelativeSide.FRONT));
        //RIGHT
        this.addRenderableWidget(new SideModeButtonWidget(this.getX() + 57, this.getY() + 41, this.config, RelativeSide.RIGHT));
        //BACK
        this.addRenderableWidget(new SideModeButtonWidget(this.getX() + 25, this.getY() + 57, this.config, RelativeSide.BACK));
        //BOTTOM
        this.addRenderableWidget(new SideModeButtonWidget(this.getX() + 41, this.getY() + 57, this.config, RelativeSide.BOTTOM));
        //AUTO-INPUT
        this.addRenderableWidget(new AutoIOModeButtonWidget(this.getX() + 18, this.getY() + 75, this.config, true));
        //AUTO-OUTPUT
        this.addRenderableWidget(new AutoIOModeButtonWidget(this.getX() + 50, this.getY() + 75, this.config, false));
        //EXIT
        this.addRenderableWidget(TexturedButton.builder(Component.translatable("custommachinery.gui.config.close"), EXIT_TEXTURE, button -> this.close())
                .bounds(this.getX() + 5, this.getY() + 5, 9, 9)
                .hovered(EXIT_TEXTURE_HOVERED)
                .build()
        );
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        blankBackground(graphics, this.getX(), this.getY(), this.xSize, this.ySize);
        graphics.drawString(Minecraft.getInstance().font, TITLE, (int)(this.getX() + this.xSize / 2F - font.width(TITLE) / 2F), this.getY() + 5, 0, false);
    }
}

package fr.frinn.custommachinery.client.screen.popup;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.widget.custom.ButtonWidget;
import fr.frinn.custommachinery.client.screen.widget.custom.config.AutoIOModeButtonWidget;
import fr.frinn.custommachinery.client.screen.widget.custom.config.SideModeButtonWidget;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class ComponentConfigPopup extends PopupScreen {

    private static final ResourceLocation EXIT_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/config/exit_button.png");
    private static final Component TITLE = new TranslatableComponent("custommachinery.gui.config.component");

    private final SideConfig config;

    public ComponentConfigPopup(SideConfig config) {
        super(96, 96);
        this.config = config;
    }

    @Override
    protected void init() {
        super.init();
        //TOP
        this.addCustomWidget(new SideModeButtonWidget(() -> this.getX() + 41, () -> this.getY() + 25, this.config, RelativeSide.TOP));
        //LEFT
        this.addCustomWidget(new SideModeButtonWidget(() -> this.getX() + 25, () -> this.getY() + 41, this.config, RelativeSide.LEFT));
        //FRONT
        this.addCustomWidget(new SideModeButtonWidget(() -> this.getX() + 41, () -> this.getY() + 41, this.config, RelativeSide.FRONT));
        //RIGHT
        this.addCustomWidget(new SideModeButtonWidget(() -> this.getX() + 57, () -> this.getY() + 41, this.config, RelativeSide.RIGHT));
        //BACK
        this.addCustomWidget(new SideModeButtonWidget(() -> this.getX() + 25, () -> this.getY() + 57, this.config, RelativeSide.BACK));
        //BOTTOM
        this.addCustomWidget(new SideModeButtonWidget(() -> this.getX() + 41, () -> this.getY() + 57, this.config, RelativeSide.BOTTOM));
        //AUTO-INPUT
        this.addCustomWidget(new AutoIOModeButtonWidget(() -> this.getX() + 18, () -> this.getY() + 75, this.config, true));
        //AUTO-OUTPUT
        this.addCustomWidget(new AutoIOModeButtonWidget(() -> this.getX() + 50, () -> this.getY() + 75, this.config, false));
        //EXIT
        this.addCustomWidget(new ButtonWidget(() -> this.getX() + 5, () -> this.getY() + 5, 9, 9)
                .texture(EXIT_TEXTURE)
                .hoverTexture(EXIT_TEXTURE, 0, 9)
                .noBackground()
                .callback(button -> this.close())
                .tooltip(new TranslatableComponent("custommachinery.gui.config.close"))
        );
    }

    @Override
    public void renderBackground(PoseStack pose) {
        blankBackground(pose, this.getX(), this.getY(), this.xSize, this.ySize);
        this.font.draw(pose, TITLE, this.getX() + this.xSize / 2F - font.width(TITLE) / 2F, this.getY() + 5, 0);
    }
}

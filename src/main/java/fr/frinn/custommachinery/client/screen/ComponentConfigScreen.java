package fr.frinn.custommachinery.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.apiimpl.component.config.RelativeSide;
import fr.frinn.custommachinery.apiimpl.component.config.SideConfig;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.client.screen.widget.SideModeButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class ComponentConfigScreen extends Screen {

    public static final int SIZE_X = 96;
    public static final int SIZE_Y = 96;
    private static final ResourceLocation TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/config/config_background.png");
    private static final ResourceLocation EXIT_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/config/exit_button.png");

    private final SideConfig config;
    private final List<SideModeButton> modeButtons;
    private int xPos;
    private int yPos;
    private ImageButton exitButton;

    public ComponentConfigScreen(SideConfig config) {
        super(new TranslatableComponent("custommachinery.gui.config.component"));
        this.config = config;
        this.modeButtons = new ArrayList<>();
    }

    @Override
    protected void init() {
        this.xPos = (this.width - SIZE_X) / 2;
        this.yPos = (this.height - SIZE_Y) / 2;
        int buttonOffsetX = this.xPos + 25;
        int buttonOffsetY = this.yPos + 25;
        //TOP
        this.modeButtons.add(this.addRenderableWidget(new SideModeButton(this, buttonOffsetX + 16, buttonOffsetY, this.config, RelativeSide.TOP)));
        //LEFT
        this.modeButtons.add(this.addRenderableWidget(new SideModeButton(this, buttonOffsetX, buttonOffsetY + 16, this.config, RelativeSide.LEFT)));
        //FRONT
        this.modeButtons.add(this.addRenderableWidget(new SideModeButton(this, buttonOffsetX + 16, buttonOffsetY + 16, this.config, RelativeSide.FRONT)));
        //RIGHT
        this.modeButtons.add(this.addRenderableWidget(new SideModeButton(this, buttonOffsetX + 32, buttonOffsetY + 16, this.config, RelativeSide.RIGHT)));
        //BACK
        this.modeButtons.add(this.addRenderableWidget(new SideModeButton(this, buttonOffsetX, buttonOffsetY + 32, this.config, RelativeSide.BACK)));
        //BOTTOM
        this.modeButtons.add(this.addRenderableWidget(new SideModeButton(this, buttonOffsetX + 16, buttonOffsetY + 32, this.config, RelativeSide.BOTTOM)));
        this.exitButton = this.addRenderableWidget(
                new ImageButton(
                    this.xPos + 5,
                    this.yPos + 5,
                    9, 9,
                    0, 0, 9,
                    EXIT_TEXTURE,
                    9, 18,
                    button -> Minecraft.getInstance().popGuiLayer(),
                    new TranslatableComponent("custommachinery.gui.config.close")
                ));
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        ClientHandler.bindTexture(TEXTURE);
        blit(pose, this.xPos, this.yPos, 0, 0, SIZE_X, SIZE_Y, SIZE_X, SIZE_Y);
        font.draw(pose, getTitle(), this.xPos + SIZE_X / 2F - font.width(getTitle()) / 2F, this.yPos + 5, 0);
        super.render(pose, mouseX, mouseY, partialTick);
        this.modeButtons.stream().filter(AbstractWidget::isHoveredOrFocused).forEach(button -> button.renderToolTip(pose, mouseX, mouseY));
        if(this.exitButton.isHoveredOrFocused())
            renderTooltip(pose, this.exitButton.getMessage(), mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
        if (super.keyPressed(keyCode, scanCode, modifiers))
            return true;
        else if (Minecraft.getInstance().options.keyInventory.isActiveAndMatches(mouseKey)) {
            this.onClose();
            return true;
        }
        else return false;
    }
}

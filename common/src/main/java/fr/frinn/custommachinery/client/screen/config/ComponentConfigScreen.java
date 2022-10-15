package fr.frinn.custommachinery.client.screen.config;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.StackableScreen;
import fr.frinn.custommachinery.client.screen.widget.AutoIOModeButton;
import fr.frinn.custommachinery.client.screen.widget.SideModeButton;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class ComponentConfigScreen extends BaseScreen implements NarratableEntry {

    public static final int SIZE_X = 96;
    public static final int SIZE_Y = 96;
    private static final ResourceLocation TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/config/config_background.png");
    private static final ResourceLocation EXIT_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/config/exit_button.png");

    private final SideConfig config;
    private final StackableScreen parent;
    private int xPos;
    private int yPos;
    public double offsetX = 0.0D;
    public double offsetY = 0.0D;

    public ComponentConfigScreen(SideConfig config, StackableScreen parent) {
        super(new TranslatableComponent("custommachinery.gui.config.component"));
        this.config = config;
        this.parent = parent;
    }

    public int getOffsetX() {
        return (int)this.offsetX;
    }

    public int getOffsetY() {
        return (int)this.offsetY;
    }

    @Override
    protected void init() {
        super.init();
        this.xPos = (this.width - SIZE_X) / 2;
        this.yPos = (this.height - SIZE_Y) / 2;
        int buttonOffsetX = this.xPos + 25;
        int buttonOffsetY = this.yPos + 25;
        //TOP
        this.addWidget(new SideModeButton(this, buttonOffsetX + 16, buttonOffsetY, this.config, RelativeSide.TOP));
        //LEFT
        this.addWidget(new SideModeButton(this, buttonOffsetX, buttonOffsetY + 16, this.config, RelativeSide.LEFT));
        //FRONT
        this.addWidget(new SideModeButton(this, buttonOffsetX + 16, buttonOffsetY + 16, this.config, RelativeSide.FRONT));
        //RIGHT
        this.addWidget(new SideModeButton(this, buttonOffsetX + 32, buttonOffsetY + 16, this.config, RelativeSide.RIGHT));
        //BACK
        this.addWidget(new SideModeButton(this, buttonOffsetX, buttonOffsetY + 32, this.config, RelativeSide.BACK));
        //BOTTOM
        this.addWidget(new SideModeButton(this, buttonOffsetX + 16, buttonOffsetY + 32, this.config, RelativeSide.BOTTOM));
        //AUTO-INPUT
        this.addWidget(new AutoIOModeButton(this, buttonOffsetX - 7, buttonOffsetY + 50, this.config, true));
        //AUTO-OUTPUT
        this.addWidget(new AutoIOModeButton(this, buttonOffsetX + 25, buttonOffsetY + 50, this.config, false));
        //EXIT
        this.addWidget(new ImageButton(
                this.xPos + 5,
                this.yPos + 5,
                9, 9,
                0, 0, 9,
                EXIT_TEXTURE,
                9, 18,
                button -> this.parent.popScreenLayer(),
                (button, pose, mouseX, mouseY) -> renderTooltip(pose, button.getMessage(), mouseX, mouseY),
                new TranslatableComponent("custommachinery.gui.config.close")
            ) {
            @Override
            public void renderButton(PoseStack poseStack, int i, int j, float f) {
                this.x = ComponentConfigScreen.this.xPos + ComponentConfigScreen.this.getOffsetX() + 5;
                this.y = ComponentConfigScreen.this.yPos + ComponentConfigScreen.this.getOffsetY() + 5;
                super.renderButton(poseStack, i, j, f);
            }
        });
    }

    @Override
    public void drawBackground(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        ClientHandler.bindTexture(TEXTURE);
        blit(pose, this.xPos + this.getOffsetX(), this.yPos + this.getOffsetY(), 0, 0, SIZE_X, SIZE_Y, SIZE_X, SIZE_Y);
        this.font.draw(pose, getTitle(), this.xPos + this.getOffsetX() + SIZE_X / 2F - font.width(getTitle()) / 2F, this.yPos + this.getOffsetY() + 5, 0);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers))
            return true;
        else if (Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode)) {
            this.parent.popScreenLayer();
            return true;
        }
        else return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for(GuiEventListener listener : this.children()) {
            if(listener.mouseClicked(mouseX, mouseY, button))
                return true;
        }
        if(mouseX >= this.xPos + this.offsetX && mouseX <= this.xPos + this.offsetX + SIZE_X && mouseY >= this.yPos + this.offsetY && mouseY <= this.yPos + this.offsetY + 20)
            setDragging(true);
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if(!isDragging())
            return false;

        this.offsetX = this.offsetX + dragX;
        this.offsetY = this.offsetY + dragY;
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        setDragging(false);
        for(GuiEventListener listener : this.children()) {
            if(listener.mouseReleased(mouseX, mouseY, button))
                return true;
        }
        return false;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {

    }
}

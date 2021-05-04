package fr.frinn.custommachinery.client.screen.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.ClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.ParametersAreNonnullByDefault;

public class TabButton extends Button {

    private static final ResourceLocation TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/creation/tabs.png");

    private boolean isSelected;

    public TabButton(int x, int y, ITextComponent title, IPressable pressedAction, ITooltip tooltip) {
        super(x, y, 28, 32, title, pressedAction, tooltip);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void renderWidget(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.enableDepthTest();
        Minecraft.getInstance().getTextureManager().bindTexture(TEXTURE);
        if(this.isSelected)
            blit(matrix, this.x, this.y, 28, 0, 28, 32, 56, 32);
        else
            blit(matrix, this.x, this.y, 0, 0, 28, 32, 56, 32);

        ClientHandler.drawCenteredString(Minecraft.getInstance().fontRenderer, matrix, getMessage().getString(), this.x + 14, this.y + 15, 0);

        if (this.isHovered()) {
            this.renderToolTip(matrix, mouseX, mouseY);
        }
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }
}

package fr.frinn.custommachinery.client.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.ClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TabButton extends Button {

    private static final ResourceLocation TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/creation/tabs.png");

    private boolean isSelected;

    public TabButton(int x, int y, Component title, OnPress pressedAction, OnTooltip tooltip) {
        super(x, y, 28, 32, title, pressedAction, tooltip);
    }

    @Override
    public void renderButton(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.enableDepthTest();
        ClientHandler.bindTexture(TEXTURE);
        if(this.isSelected)
            blit(matrix, this.x, this.y, 28, 0, 28, 32, 56, 32);
        else
            blit(matrix, this.x, this.y, 0, 0, 28, 32, 56, 32);

        ClientHandler.drawCenteredString(Minecraft.getInstance().font, matrix, getMessage().getString(), this.x + 14, this.y + 15, 0);

        if (this.isHovered) {
            this.renderToolTip(matrix, mouseX, mouseY);
        }
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }
}

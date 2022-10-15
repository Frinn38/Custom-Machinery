package fr.frinn.custommachinery.client.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.client.ClientHandler;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TexturedButton extends Button {

    private ResourceLocation texture;

    public TexturedButton(int x, int y, int width, int height, Component title, ResourceLocation texture, OnPress pressedAction, OnTooltip onTooltip) {
        super(x, y, width, height, title, pressedAction, onTooltip);
        this.texture = texture;
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        ClientHandler.bindTexture(this.texture);
        blit(pose, this.x, this.y, this.width, this.height, 0, 0, this.width, this.height, this.width, this.height);
    }
}

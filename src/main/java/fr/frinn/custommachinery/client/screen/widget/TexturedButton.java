package fr.frinn.custommachinery.client.screen.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.ParametersAreNonnullByDefault;

public class TexturedButton extends Button {

    private ResourceLocation texture;

    public TexturedButton(int x, int y, int width, int height, ITextComponent title, ResourceLocation texture, IPressable pressedAction, ITooltip onTooltip) {
        super(x, y, width, height, title, pressedAction, onTooltip);
        this.texture = texture;
    }

    @ParametersAreNonnullByDefault
    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.render(matrix, mouseX, mouseY, partialTicks);
        Minecraft.getInstance().getTextureManager().bindTexture(this.texture);
        blit(matrix, this.x, this.y, this.width, this.height, 0, 0, this.width, this.height, this.width, this.height);
    }
}

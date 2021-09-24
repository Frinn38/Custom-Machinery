package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.api.guielement.IGuiElementRenderer;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.data.gui.ResetGuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;

public class ResetGuiElementRenderer implements IGuiElementRenderer<ResetGuiElement> {

    @Override
    public void renderElement(MatrixStack matrix, ResetGuiElement element, IMachineScreen screen) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();

        Minecraft.getInstance().getTextureManager().bindTexture(element.getTexture());
        AbstractGui.blit(matrix, posX, posY, 0, 0, width, height, width, height);
    }

    @Override
    public void renderTooltip(MatrixStack matrix, ResetGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {
        List<ITextComponent> tooltips = new ArrayList<>();
        tooltips.add(new TranslationTextComponent("custommachinery.gui.element.reset.name"));
        tooltips.add(new TranslationTextComponent("custommachinery.gui.element.reset.tooltip").mergeStyle(TextFormatting.DARK_RED));
        screen.getScreen().func_243308_b(matrix, tooltips, mouseX, mouseY);
    }

    @Override
    public boolean isHovered(ResetGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();

        return mouseX >= posX && mouseX <= posX + width && mouseY >= posY && mouseY <= posY + height;
    }
}

package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.common.data.gui.SlotGuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class SlotGuiElementRenderer implements IGuiElementRenderer<SlotGuiElement> {

    @Override
    public void renderElement(MatrixStack matrix, SlotGuiElement element, CustomMachineScreen screen) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();

        Minecraft.getInstance().getTextureManager().bindTexture(element.getTexture());
        AbstractGui.blit(matrix, posX, posY, 0, 0, width, height, width, height);
        if(element.getItem() != Items.AIR) {
            screen.renderTransparentItem(matrix, new ItemStack(element.getItem()), posX + 1, posY + 1);
        }
    }

    @Override
    public void renderTooltip(MatrixStack matrix, SlotGuiElement element, CustomMachineScreen screen, int mouseX, int mouseY) {

    }

    @Override
    public boolean isHovered(SlotGuiElement element, CustomMachineScreen screen, int mouseX, int mouseY) {
        int posX = element.getX();
        int posY = element.getY();

        int width = element.getWidth();
        int height = element.getHeight();
        return mouseX >= posX && mouseX <= posX + width && mouseY >= posY && mouseY <= posY + height;
    }
}

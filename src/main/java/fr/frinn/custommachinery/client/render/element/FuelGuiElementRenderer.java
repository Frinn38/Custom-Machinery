package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.common.data.component.FuelMachineComponent;
import fr.frinn.custommachinery.common.data.gui.FuelGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.text.TranslationTextComponent;

public class FuelGuiElementRenderer implements IGuiElementRenderer<FuelGuiElement> {

    @Override
    public void renderElement(MatrixStack matrix, FuelGuiElement element, CustomMachineScreen screen) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();
        screen.getMinecraft().getTextureManager().bindTexture(element.getEmptyTexture());
        AbstractGui.blit(matrix, posX, posY, 0, 0, width, height, width, height);
        int fuel = screen.getTile().componentManager.getComponent(Registration.FUEL_MACHINE_COMPONENT.get()).map(FuelMachineComponent::getFuel).orElse(0);
        int maxFuel = screen.getTile().componentManager.getComponent(Registration.FUEL_MACHINE_COMPONENT.get()).map(FuelMachineComponent::getMaxFuel).orElse(0);
        if(fuel != 0 && maxFuel != 0) {
            double filledPercent = (double)fuel / (double)maxFuel;
            screen.getMinecraft().textureManager.bindTexture(element.getFilledTexture());
            AbstractGui.blit(matrix, posX, posY + (int)(height *  (1 - filledPercent)), 0, (int)(height * (1 - filledPercent)), width, (int)(height * filledPercent), width, height);
        }
    }

    @Override
    public void renderTooltip(MatrixStack matrix, FuelGuiElement element, CustomMachineScreen screen, int mouseX, int mouseY) {
        int fuel = screen.getTile().componentManager.getComponent(Registration.FUEL_MACHINE_COMPONENT.get()).map(FuelMachineComponent::getFuel).orElse(0);
        screen.renderTooltip(matrix, new TranslationTextComponent("custommachinery.gui.element.fuel.tooltip", fuel), mouseX, mouseY);
    }

    @Override
    public boolean isHovered(FuelGuiElement element, CustomMachineScreen screen, int mouseX, int mouseY) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();
        return mouseX >= posX && mouseX <= posX + width && mouseY >= posY && mouseY <= posY + height;
    }
}

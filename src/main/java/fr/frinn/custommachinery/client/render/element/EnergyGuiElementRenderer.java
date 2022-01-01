package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.api.guielement.IGuiElementRenderer;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.data.gui.EnergyGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.text.TranslationTextComponent;

public class EnergyGuiElementRenderer implements IGuiElementRenderer<EnergyGuiElement> {

    @Override
    public void renderElement(MatrixStack matrix, EnergyGuiElement element, IMachineScreen screen) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();
        Minecraft.getInstance().getTextureManager().bindTexture(element.getEmptyTexture());
        AbstractGui.blit(matrix, posX, posY, 0, 0, width, height, width, height);
        screen.getTile().getComponentManager().getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).ifPresent(energy -> {
            double fillPercent = (double)energy.getEnergyStored() / (double)energy.getMaxEnergyStored();
            int eneryHeight = (int)(fillPercent * (double)(height));
            Minecraft.getInstance().getTextureManager().bindTexture(element.getFilledTexture());
            AbstractGui.blit(matrix, posX, posY + height - eneryHeight, 0, height - eneryHeight, width, eneryHeight, width, height);
        });
    }

    @Override
    public void renderTooltip(MatrixStack matrix, EnergyGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {
        screen.getTile().getComponentManager().getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).ifPresent(energyComponent -> {
            int energy = energyComponent.getEnergyStored();
            int maxEnergy = energyComponent.getMaxEnergyStored();
            screen.getScreen().renderTooltip(matrix, new TranslationTextComponent("custommachinery.gui.element.energy.tooltip", energy, maxEnergy), mouseX, mouseY);
        });
    }
}

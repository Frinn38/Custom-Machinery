package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.guielement.IGuiElementRenderer;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.data.gui.EnergyGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class EnergyGuiElementRenderer implements IGuiElementRenderer<EnergyGuiElement> {

    @Override
    public void renderElement(PoseStack matrix, EnergyGuiElement element, IMachineScreen screen) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();
        ClientHandler.bindTexture(element.getEmptyTexture());
        GuiComponent.blit(matrix, posX, posY, 0, 0, width, height, width, height);
        screen.getTile().getComponentManager().getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).ifPresent(energy -> {
            double fillPercent = energy.getFillPercent();
            int eneryHeight = (int)(fillPercent * (double)(height));
            ClientHandler.bindTexture(element.getFilledTexture());
            GuiComponent.blit(matrix, posX, posY + height - eneryHeight, 0, height - eneryHeight, width, eneryHeight, width, height);
        });
    }

    @Override
    public void renderTooltip(PoseStack matrix, EnergyGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {
        screen.getTile().getComponentManager().getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).ifPresent(energyComponent -> {
            long energy = energyComponent.getEnergy();
            long maxEnergy = energyComponent.getCapacity();
            screen.getScreen().renderTooltip(matrix, new TranslatableComponent("custommachinery.gui.element.energy.tooltip", energy, maxEnergy), mouseX, mouseY);
        });
    }
}

package fr.frinn.custommachinery.client.integration.jei.element;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.integration.jei.IJEIElementRenderer;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.guielement.ExperienceGuiElement;
import net.minecraft.client.gui.GuiComponent;

public class ExperienceGuiElementJeiRenderer implements IJEIElementRenderer<ExperienceGuiElement> {

  @Override
  public void renderElementInJEI(PoseStack matrix, ExperienceGuiElement element, IMachineRecipe recipe, int mouseX, int mouseY) {
    int posX = element.getX();
    int posY = element.getY();
    int width = element.getWidth();
    int height = element.getHeight();
    if (!element.getMode().isDisplayBar()) {
      ClientHandler.bindTexture(element.getTexture());
      GuiComponent.blit(matrix, posX, posY, 0, 0, width, height, width, height);
    }
  }
}

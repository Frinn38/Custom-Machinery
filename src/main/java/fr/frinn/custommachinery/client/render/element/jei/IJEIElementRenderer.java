package fr.frinn.custommachinery.client.render.element.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.common.crafting.CustomMachineRecipe;
import fr.frinn.custommachinery.common.data.gui.IGuiElement;

public interface IJEIElementRenderer<T extends IGuiElement> {

    void renderElementInJEI(MatrixStack matrix, T element, CustomMachineRecipe recipe, int mouseX, int mouseY);
}

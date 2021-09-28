package fr.frinn.custommachinery.api.guielement.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.recipe.IMachineRecipe;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

/**
 * Implements this interface if you want your Gui Element to render in JEI recipe.
 * @param <T> The Gui Element to render.
 */
public interface IJEIElementRenderer<T extends IGuiElement> {

    /**
     * Called each frame on client side for each gui element on each currently displayed recipes.
     * Render your element here.
     */
    void renderElementInJEI(MatrixStack matrix, T element, IMachineRecipe recipe, int mouseX, int mouseY);

    /**
     * Called to check if the mouse cursor currently hover this element on a jei recipe.
     * If this method return true the element tooltips returned by getJeiTooltips method will be rendered at mouse cursor position.
     */
    default boolean isHoveredInJei(T element, int posX, int posY, int mouseX, int mouseY) {
        return mouseX >= posX && mouseX <= posX + element.getWidth() && mouseY >= posY && mouseY <= posY + element.getHeight();
    }

    /**
     * @return A list of text components that will be displayed as tooltips when the mouse cursor hover the gui element.
     */
    List<ITextComponent> getJEITooltips(T element, IMachineRecipe recipe);
}

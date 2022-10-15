package fr.frinn.custommachinery.api.integration.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import net.minecraft.network.chat.Component;

import java.util.Collections;
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
    void renderElementInJEI(PoseStack matrix, T element, IMachineRecipe recipe, int mouseX, int mouseY);

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
    default List<Component> getJEITooltips(T element, IMachineRecipe recipe) {
        return Collections.emptyList();
    }
}

package fr.frinn.custommachinery.api.guielement;

import com.mojang.blaze3d.matrix.MatrixStack;

/**
 * Used to handle rendering for all IGuiElement instances of a specific GuiElementType.
 * Register an IGuiElementRenderer using RegisterGuiElementRendererEvent.
 * All registered GuiElementType must have an IGuiElementRenderer or nothing will show in the machine gui.
 * @param <E> The IGuiElement to render.
 */
public interface IGuiElementRenderer<E extends IGuiElement> {

    /**
     * Called each frame for each gui element of the corresponding type.
     * Render your element (text, texture, item, etc..) here.
     * The MatrixStack is translated to the top left of the machine gui, consider it the 0,0 point for the rendering.
     */
    void renderElement(MatrixStack matrix, E element, IMachineScreen screen);

    /**
     * Called each frame for each gui element of the corresponding type that return true to isHovered.
     * Render a tooltip here.
     * The MatrixStack is translated to the top left of the machine gui, consider it the 0,0 point for the rendering.
     */
    void renderTooltip(MatrixStack matrix, E element, IMachineScreen screen, int mouseX, int mouseY);

    /**
     * Calculate if the mouse cursor is hovering the element and return true if so.
     */
    default boolean isHovered(E element, IMachineScreen screen, int mouseX, int mouseY) {
        return mouseX >= element.getX() && mouseX <= element.getX() + element.getWidth() && mouseY >= element.getY() && mouseY <= element.getY() + element.getHeight();
    }
}

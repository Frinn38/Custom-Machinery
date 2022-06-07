package fr.frinn.custommachinery.api.guielement;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.api.codec.CodecLogger;
import fr.frinn.custommachinery.api.machine.MachineTile;

/**
 * A part of the Custom Machine data, used to display things on the machine gui and/or in jei.
 * All gui elements are parsed from the machine json gui property, then created using the Codec passed when registering the corresponding GuiElementType.
 * As gui elements are part of the machine data, the must exist on both sides, so no rendering or other client side only things are allowed here.
 * This class only hold data, the rendering is handled by the IGuiElementRenderer.
 * Each IGuiElement must have a GuiElementType registered to the forge registry.
 */
public interface IGuiElement {

    /**
     * A dispatch codec, used to create all IGuiElement from the machine json.
     */
    Codec<IGuiElement> CODEC = CodecLogger.loggedDispatch(
            ICustomMachineryAPI.INSTANCE.registryCodec(ICustomMachineryAPI.INSTANCE.guiElementRegistry(), true),
            IGuiElement::getType,
            GuiElementType::getCodec,
            "Gui Element"
    );

    /**
     * @return A registered GuiElementType corresponding to this IGuiElement.
     */
    GuiElementType<? extends IGuiElement> getType();

    /**
     * @return The X pos of the element in pixel (horizontal, 0 is the left of the machine gui)
     */
    int getX();

    /**
     * @return The Y pos of the element in pixel (vertical, 0 is the top of the machine gui)
     */
    int getY();

    /**
     * @return The width in pixel of the element, used to scale the textures and calculate if the mouse cursor is hovering the element.
     */
    int getWidth();

    /**
     * @return The height in pixel of the element, used to scale the textures and calculate if the mouse cursor is hovering the element.
     */
    int getHeight();

    /**
     * @return The priority for rendering the element. If several elements overlap, the elements with higher priority are rendered on top of those with lower priority.
     */
    int getPriority();

    /**
     * Called server-side when player click on a gui element.
     * @param button The mouse button that was clicked.
     *               0 : left
     *               1 : right
     *               2 : middle
     * @param tile The machine the player is currently using.
     */
    void handleClick(byte button, MachineTile tile);
}

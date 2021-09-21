package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.api.utils.CodecLogger;
import fr.frinn.custommachinery.common.util.Codecs;

public interface IGuiElement {

    Codec<IGuiElement> CODEC = CodecLogger.loggedDispatch(Codecs.GUI_ELEMENT_TYPE, IGuiElement::getType, GuiElementType::getCodec, "Gui Element");

    GuiElementType<? extends IGuiElement> getType();

    int getX();

    int getY();

    int getWidth();

    int getHeight();

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

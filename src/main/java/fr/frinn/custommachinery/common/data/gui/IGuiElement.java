package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
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
}

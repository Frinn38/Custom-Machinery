package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.common.util.Codecs;

public interface IGuiElement {

    Codec<IGuiElement> CODEC = Codecs.GUI_ELEMENT_TYPE_CODEC.dispatch("type",IGuiElement::getType, GuiElementType::getCodec);

    GuiElementType getType();

    int getX();

    int getY();

    int getWidth();

    int getHeight();

    int getPriority();
}

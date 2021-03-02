package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;

public interface IGuiElement {

    Codec<IGuiElement> CODEC = GuiElementType.CODEC.dispatch("type", IGuiElement::getType, GuiElementType::getCodec);

    GuiElementType getType();

    int getX();

    int getY();

    int getWidth();

    int getHeight();

    int getPriority();
}

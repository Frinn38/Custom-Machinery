package fr.frinn.custommachinery.apiimpl.guielement;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.apiimpl.codec.CodecLogger;

public abstract class AbstractGuiElement implements IGuiElement {

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final int priority;

    public AbstractGuiElement(int x, int y, int width, int height, int priority) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.priority = priority;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public void handleClick(byte button, MachineTile tile) {

    }

    public static <T extends AbstractGuiElement> Products.P5<RecordCodecBuilder.Mu<T>, Integer, Integer, Integer, Integer, Integer> makeBaseCodec(RecordCodecBuilder.Instance<T> guiElement) {
        return guiElement.group(
                Codec.intRange(0, Integer.MAX_VALUE).fieldOf("x").forGetter(AbstractGuiElement::getX),
                Codec.intRange(0, Integer.MAX_VALUE).fieldOf("y").forGetter(AbstractGuiElement::getY),
                CodecLogger.loggedOptional(Codec.intRange(-1, Integer.MAX_VALUE),"width", -1).forGetter(AbstractGuiElement::getWidth),
                CodecLogger.loggedOptional(Codec.intRange(-1, Integer.MAX_VALUE),"height", -1).forGetter(AbstractGuiElement::getHeight),
                CodecLogger.loggedOptional(Codec.INT,"priority", 0).forGetter(AbstractGuiElement::getPriority)
        );
    }
}

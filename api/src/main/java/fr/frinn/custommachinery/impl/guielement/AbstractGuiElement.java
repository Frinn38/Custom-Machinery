package fr.frinn.custommachinery.impl.guielement;

import com.mojang.datafixers.Products;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.impl.codec.NamedRecordCodec;

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

    public static <T extends AbstractGuiElement> Products.P5<NamedRecordCodec.Mu<T>, Integer, Integer, Integer, Integer, Integer> makeBaseCodec(NamedRecordCodec.Instance<T> guiElement) {
        return guiElement.group(
                NamedCodec.intRange(0, Integer.MAX_VALUE).fieldOf("x").forGetter(AbstractGuiElement::getX),
                NamedCodec.intRange(0, Integer.MAX_VALUE).fieldOf("y").forGetter(AbstractGuiElement::getY),
                NamedCodec.intRange(-1, Integer.MAX_VALUE).optionalFieldOf("width", -1).forGetter(AbstractGuiElement::getWidth),
                NamedCodec.intRange(-1, Integer.MAX_VALUE).optionalFieldOf("height", -1).forGetter(AbstractGuiElement::getHeight),
                NamedCodec.INT.optionalFieldOf("priority", 0).forGetter(AbstractGuiElement::getPriority)
        );
    }
}

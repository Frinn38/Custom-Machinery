package fr.frinn.custommachinery.apiimpl.guielement;

import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.machine.MachineTile;

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
}

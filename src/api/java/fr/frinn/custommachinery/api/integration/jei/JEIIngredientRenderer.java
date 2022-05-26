package fr.frinn.custommachinery.api.integration.jei;

import fr.frinn.custommachinery.api.guielement.IGuiElement;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;

public abstract class JEIIngredientRenderer<T, E extends IGuiElement> implements IIngredientRenderer<T> {

    public final E element;

    public JEIIngredientRenderer(E element) {
        this.element = element;
    }

    public E getElement() {
        return this.element;
    }

    public abstract IIngredientType<T> getType();
}

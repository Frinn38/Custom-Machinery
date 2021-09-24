package fr.frinn.custommachinery.api.component;

import java.util.function.Predicate;

/**
 * Used by the JEI integration to check if an ingredient can be displayed in this component in the machine gui.
 * Example : ItemComponent can filter which items can be in it's slot.
 */
public interface IFilterComponent extends IMachineComponent {

    /**
     * The Object can be an ingredient or a list of ingredient
     * @return A Prediacte to check if the ingredient is allowed to be in this component.
     * If the predicate return false, the ingredient will not be displayed in this component on the recipe gui.
     */
    Predicate<Object> getFilter();
}

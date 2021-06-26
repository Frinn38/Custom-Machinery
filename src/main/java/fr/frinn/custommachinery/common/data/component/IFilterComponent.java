package fr.frinn.custommachinery.common.data.component;

import java.util.function.Predicate;

public interface IFilterComponent {

    //The Object can be an ingredient or a list of ingredient
    Predicate<Object> getFilter();
}

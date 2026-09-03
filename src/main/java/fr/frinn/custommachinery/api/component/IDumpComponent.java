package fr.frinn.custommachinery.api.component;

import java.util.List;

/**
 * Used to define a custom dumping behavior for this component.
 */
public interface IDumpComponent extends IMachineComponent {

    /**
     * Will be called by the dump gui element when a player click on it, usually it should void the content of the component.
     * @param ids The ids of the components to dump, in case the IDumpComponent is an {@link fr.frinn.custommachinery.api.component.handler.IComponentHandler}
     */
    void dump(List<String> ids);
}

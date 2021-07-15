package fr.frinn.custommachinery.api.components;

/**
 * Used to define a custom comparator behaviour for this component.
 */
public interface IComparatorInputComponent extends IMachineComponent {

    /**
     * @return The redstone signal power (between 0 and 15 included) that the redstone comparator will emit.
     */
    int getComparatorInput();
}

package fr.frinn.custommachinery.api.component;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.impl.codec.RegistrarCodec;

/**
 * A template for a specific {@link MachineComponentType}.
 * This template will be created on datapack reload using its codec to deserialize the data put inside the machine JSON.
 * The template will then be hold inside the {@link fr.frinn.custommachinery.api.machine.ICustomMachine} instance and be used to create new instances of {@link IMachineComponent}
 * when a {@link fr.frinn.custommachinery.api.machine.MachineTile} is set to a {@link fr.frinn.custommachinery.api.machine.ICustomMachine} holding this template.
 * @param <T> The {@link IMachineComponent} that this template will create.
 */
public interface IMachineComponentTemplate<T extends IMachineComponent> {

    /**
     * A dispatch codec, this codec will be used to deserialize the array of JSON objects in the "components" property of the machine JSON.
     * The dispatch codec will read the "type" property inside the component JSON and find the proper {@link MachineComponentType} for this type.
     * The codec passed to the {@link MachineComponentType} on registration will then be used to deserialize the component JSON into the template.
     */
    NamedCodec<IMachineComponentTemplate<? extends IMachineComponent>> CODEC = RegistrarCodec.MACHINE_COMPONENT.dispatch(
            IMachineComponentTemplate::getType,
            MachineComponentType::getCodec,
            "Machine Component"
    );

    /**
     * Used by the dispatch codec.
     * @return The {@link MachineComponentType} registered for this {@link IMachineComponent}.
     * It MUST be the same instance as the one registered in the registry.
     */
    MachineComponentType<T> getType();

    /**
     * Used by the JEI integration to find a specific {@link IMachineComponent} from a {@link fr.frinn.custommachinery.api.guielement.IGuiElement}.
     * For example, to display an item in the correct slot in a jei recipe.
     * @return The string ID of this {@link IMachineComponent}, or "" if this {@link MachineComponentType} is unique or don't have an ID.
     */
    String getId();

    /**
     * Used by the JEI integration to find a specific {@link IMachineComponent} from a {@link fr.frinn.custommachinery.api.guielement.IGuiElement}.
     * For example, to display an item in the correct slot in a jei recipe.
     * @param ingredient The ingredient (ItemStack, FluidStack...) JEI is trying to fit in that component. Can be a List<>
     * @param isInput True if the {@link IMachineComponent} MUST be an input. False if the component MUST be an output.
     * @param manager A dummy {@link IMachineComponentManager} (not linked to any real {@link fr.frinn.custommachinery.api.machine.MachineTile}) used for some checks that needs a machine context.
     * @return True if this ingredient can be put in this {@link IMachineComponent}, false otherwise.
     * If true is returned by this method, the JEI integration will assume that this component can't accept another ingredient and will not check it for remaining recipe ingredients.
     */
    boolean canAccept(Object ingredient, boolean isInput, IMachineComponentManager manager);

    /**
     * Create a new {@link IMachineComponent} using this template.
     * @param manager The {@link IMachineComponentManager} that will hold this {@link IMachineComponent}.
     * @return The created {@link IMachineComponent}.
     */
    T build(IMachineComponentManager manager);
}

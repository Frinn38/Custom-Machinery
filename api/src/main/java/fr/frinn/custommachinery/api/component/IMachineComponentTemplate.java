package fr.frinn.custommachinery.api.component;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.impl.codec.RegistrarCodec;

/**
 * A template for a specific MachineComponentType.
 * This template will be created on datapack reload using it's coded to deserialize the data put inside the machine json.
 * The template will then be hold inside the CustomMachine instance and be used to create new instances of IMachineComponent
 * when a MachineTile is set to a CustomMachine holding this template.
 * @param <T> The component that this template will create.
 */
public interface IMachineComponentTemplate<T extends IMachineComponent> {

    /**
     * A dispatch codec, this codec will be used to deserialize the array of json objects in the "components" property of the machine json.
     * The dispatch codec will read the "type" property inside the component json and find the proper MachineComponentType for this type.
     * The codec passed to the MachineComponentType on registration will then be used to deserialize the component json into the template.
     */
    NamedCodec<IMachineComponentTemplate<? extends IMachineComponent>> CODEC = RegistrarCodec.MACHINE_COMPONENT.dispatch(
            IMachineComponentTemplate::getType,
            MachineComponentType::getCodec,
            "Machine Component"
    );

    /**
     * Used by the dispatch codec.
     * @return The MachineComponentType registered for this component.
     * It MUST be the same instance as the one registered in the forge registry.
     */
    MachineComponentType<T> getType();

    /**
     * Used by the JEI integration to find a specific component from a GuiElement.
     * For example, to display an item in the correct slot in a jei recipe.
     * @return The string ID of this component, or "" if this component type is unique or don't have an ID.
     */
    String getId();

    /**
     * Used by the JEI integration to find a specific component from a GuiElement.
     * For example, to display an item in the correct slot in a jei recipe.
     * @param ingredient The ingredient (ItemStack, FluidStack...) JEI is trying to fit in that component. Can be a List<>
     * @param isInput True if the component MUST be an input. False if the component MUST be an output.
     * @param manager A dummy IMachineComponentManager (not linked to any real IMachineTile) used for some checks that needs a machine context.
     * @return True if this ingredient can be put in this component, false otherwise.
     * If true is returned by this method, the JEI integration will assume that this component can't accept another ingredient and will not check it for remaining recipe ingredients.
     */
    boolean canAccept(Object ingredient, boolean isInput, IMachineComponentManager manager);

    /**
     * Create a new IMachineComponent using this template.
     * @param manager The IMachineComponentManager that will hold this component.
     * @return The created ImachineComponent.
     */
    T build(IMachineComponentManager manager);
}

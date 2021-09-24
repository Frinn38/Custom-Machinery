package fr.frinn.custommachinery.api.component;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.api.utils.CodecLogger;
import fr.frinn.custommachinery.api.utils.RegistryCodec;

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
    Codec<IMachineComponentTemplate<? extends IMachineComponent>> CODEC = CodecLogger.loggedDispatch(RegistryCodec.MACHINE_COMPONENT_TYPE, IMachineComponentTemplate::getType, MachineComponentType::getCodec, "Machine Component");

    /**
     * Used by the dispatch codec.
     * @return The MachineComponentType registered for this component.
     * It MUST be the same instance as the one registered in the forge registry.
     */
    MachineComponentType<T> getType();

    /**
     * Create a new IMachineComponent using this template.
     * @param manager The IMachineComponentManager that will hold this component.
     * @return The created ImachineComponent.
     */
    T build(IMachineComponentManager manager);
}

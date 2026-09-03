package fr.frinn.custommachinery.api.crafting;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.impl.codec.RegistrarCodec;

/**
 * A template for a specific {@link ProcessorType}.
 * This template will be created on datapack reload using its codec to deserialize the data put inside the machine JSON.
 * The template will then be hold inside the {@link fr.frinn.custommachinery.api.machine.ICustomMachine} instance and be used to create new instances of {@link IProcessor}
 * when a {@link MachineTile} is set to a {@link fr.frinn.custommachinery.api.machine.ICustomMachine} holding this template.
 * @param <T> The {@link IProcessor} that this template will create.
 */
public interface IProcessorTemplate<T extends IProcessor> {

    /**
     * A dispatch codec, this codec will be used to deserialize the array of JSON objects in the "processor" property of the machine JSON.
     * The dispatch codec will read the "type" property inside the processor JSON and find the proper {@link ProcessorType} for this type.
     * The codec passed to the {@link ProcessorType} on registration will then be used to deserialize the processor JSON into the template.
     */
    NamedCodec<IProcessorTemplate<? extends IProcessor>> CODEC = RegistrarCodec.CRAFTING_PROCESSOR.dispatch(
            IProcessorTemplate::getType,
            ProcessorType::getCodec,
            "Crafting Processor"
    );

    /**
     * Used by the dispatch codec.
     * @return The {@link ProcessorType} registered for this {@link IProcessor}.
     * It MUST be the same instance as the one registered in the registry.
     */
    ProcessorType<T> getType();

    /**
     * Create a new {@link IProcessor} using this template.
     * @param tile The {@link MachineTile} that will hold this {@link IProcessor}.
     * @return The created {@link IProcessor}.
     */
    T build(MachineTile tile);
}

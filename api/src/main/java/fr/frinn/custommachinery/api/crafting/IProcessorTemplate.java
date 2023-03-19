package fr.frinn.custommachinery.api.crafting;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.impl.codec.RegistrarCodec;

public interface IProcessorTemplate<T extends IProcessor> {

    NamedCodec<IProcessorTemplate<? extends IProcessor>> CODEC = RegistrarCodec.CRAFTING_PROCESSOR.dispatch(
            IProcessorTemplate::getType,
            ProcessorType::getCodec,
            "Crafting Processor"
    );

    ProcessorType<T> getType();

    T build(MachineTile tile);
}

package fr.frinn.custommachinery.api.crafting;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.impl.codec.CodecLogger;
import fr.frinn.custommachinery.impl.codec.RegistrarCodec;

public interface IProcessorTemplate<T extends IProcessor> {

    Codec<IProcessorTemplate<? extends IProcessor>> CODEC = CodecLogger.loggedDispatch(
            RegistrarCodec.CRAFTING_PROCESSOR,
            IProcessorTemplate::getType,
            ProcessorType::getCodec,
            "Crafting Processor"
    );

    ProcessorType<T> getType();

    T build(MachineTile tile);
}

package fr.frinn.custommachinery.common.data.component;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.common.util.Codecs;

public interface IMachineComponentTemplate<T extends IMachineComponent> {

    Codec<IMachineComponentTemplate<? extends IMachineComponent>> CODEC = Codecs.MACHINE_COMPONENT_TYPE_CODEC.dispatch("type",IMachineComponentTemplate::getType, MachineComponentType::getCodec);

    MachineComponentType<T> getType();

    T build(MachineComponentManager manager);
}

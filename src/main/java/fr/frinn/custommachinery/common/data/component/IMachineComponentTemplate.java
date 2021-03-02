package fr.frinn.custommachinery.common.data.component;

import com.mojang.serialization.Codec;

public interface IMachineComponentTemplate<T extends IMachineComponent> {

    Codec<IMachineComponentTemplate<? extends IMachineComponent>> CODEC = MachineComponentType.CODEC.dispatch("type", IMachineComponentTemplate::getType, MachineComponentType::getCodec);

    MachineComponentType<T> getType();

    T build(MachineComponentManager manager);
}

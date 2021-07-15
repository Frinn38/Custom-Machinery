package fr.frinn.custommachinery.api.components.builder;

import fr.frinn.custommachinery.api.components.IMachineComponent;
import fr.frinn.custommachinery.api.components.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.components.MachineComponentType;

import java.util.List;

public interface IMachineComponentBuilder<T extends IMachineComponent> {

    IMachineComponentBuilder<T> fromComponent(IMachineComponent component);

    MachineComponentType<T> getType();

    List<IComponentBuilderProperty<?>> getProperties();

    IMachineComponentTemplate<T> build();
}

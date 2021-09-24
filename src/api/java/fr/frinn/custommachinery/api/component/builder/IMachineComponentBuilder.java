package fr.frinn.custommachinery.api.component.builder;

import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.component.MachineComponentType;

import java.util.List;

public interface IMachineComponentBuilder<T extends IMachineComponent> {

    IMachineComponentBuilder<T> fromComponent(IMachineComponent component);

    MachineComponentType<T> getType();

    List<IComponentBuilderProperty<?>> getProperties();

    IMachineComponentTemplate<T> build();
}

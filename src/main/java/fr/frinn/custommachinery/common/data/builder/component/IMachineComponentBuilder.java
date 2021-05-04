package fr.frinn.custommachinery.common.data.builder.component;

import fr.frinn.custommachinery.common.data.builder.component.property.IComponentBuilderProperty;
import fr.frinn.custommachinery.common.data.component.IMachineComponent;
import fr.frinn.custommachinery.common.data.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;

import java.util.List;

public interface IMachineComponentBuilder<T extends IMachineComponent> {

    IMachineComponentBuilder<T> fromComponent(IMachineComponent component);

    MachineComponentType<T> getType();

    List<IComponentBuilderProperty<?>> getProperties();

    IMachineComponentTemplate<T> build();
}

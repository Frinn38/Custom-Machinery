package fr.frinn.custommachinery.common.machine.builder.component;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.component.builder.IComponentBuilderProperty;
import fr.frinn.custommachinery.api.component.builder.IMachineComponentBuilder;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.component.variant.item.DefaultItemComponentVariant;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.component.builder.IntComponentBuilderProperty;
import fr.frinn.custommachinery.impl.component.builder.ModeComponentBuilderProperty;
import fr.frinn.custommachinery.impl.component.builder.StringComponentBuilderProperty;

import java.util.ArrayList;
import java.util.List;

public class ItemComponentBuilder implements IMachineComponentBuilder<ItemMachineComponent> {

    private StringComponentBuilderProperty id = new StringComponentBuilderProperty("id", "");
    private IntComponentBuilderProperty capacity = new IntComponentBuilderProperty("capacity", 64);
    private ModeComponentBuilderProperty mode = new ModeComponentBuilderProperty("mode", ComponentIOMode.BOTH);
    private List<IComponentBuilderProperty<?>> properties = Lists.newArrayList(id, capacity, mode);

    public ItemComponentBuilder fromComponent(IMachineComponent component) {
        if(component instanceof ItemMachineComponent) {
            ItemMachineComponent itemComponent = (ItemMachineComponent)component;
            this.id.set(itemComponent.getId());
            this.capacity.set(itemComponent.getCapacity());
            this.mode.set(itemComponent.getMode());
        }
        return this;
    }

    @Override
    public MachineComponentType<ItemMachineComponent> getType() {
        return Registration.ITEM_MACHINE_COMPONENT.get();
    }

    @Override
    public List<IComponentBuilderProperty<?>> getProperties() {
        return this.properties;
    }

    @Override
    public IMachineComponentTemplate<ItemMachineComponent> build() {
        return new ItemMachineComponent.Template(id.get(), mode.get(), capacity.get(), capacity.get(), capacity.get(), new ArrayList<>(), false, DefaultItemComponentVariant.INSTANCE, null, false);
    }
}

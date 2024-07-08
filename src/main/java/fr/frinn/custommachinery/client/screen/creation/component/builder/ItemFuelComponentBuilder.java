package fr.frinn.custommachinery.client.screen.creation.component.builder;

import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.utils.Filter;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.common.component.item.FuelItemMachineComponent;
import fr.frinn.custommachinery.common.component.item.ItemMachineComponent;
import fr.frinn.custommachinery.common.component.item.ItemMachineComponent.Template;
import fr.frinn.custommachinery.common.init.Registration;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

public class ItemFuelComponentBuilder extends ItemComponentBuilder {

    @Override
    public MachineComponentType<ItemMachineComponent> type() {
        return Registration.ITEM_FUEL_MACHINE_COMPONENT.get();
    }

    @Override
    public PopupScreen makePopup(MachineEditScreen parent, @Nullable ItemMachineComponent.Template template, Consumer<Template> onFinish) {
        return new ItemFuelComponentBuilderPopup(parent, template, onFinish);
    }

    public static class ItemFuelComponentBuilderPopup extends ItemComponentBuilderPopup {

        public ItemFuelComponentBuilderPopup(BaseScreen parent, @Nullable Template template, Consumer<Template> onFinish) {
            super(parent, template, onFinish);
        }

        @Override
        public Template makeTemplate() {
            return new FuelItemMachineComponent.Template(this.id.getValue(), this.mode.getValue(), this.capacity.intValue(), Optional.of(this.maxInput.intValue()), Optional.of(this.maxOutput.intValue()), this.baseTemplate().map(template -> template.filter).orElse(Filter.empty()), Optional.of(this.mode.getValue().getBaseConfig()), this.locked.selected());
        }
    }
}

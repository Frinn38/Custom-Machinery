package fr.frinn.custommachinery.client.screen.creation.component.builder;

import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.common.component.item.ItemMachineComponent;
import fr.frinn.custommachinery.common.component.item.ItemMachineComponent.Template;
import fr.frinn.custommachinery.common.component.item.UpgradeItemMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

public class ItemUpgradeComponentBuilder extends ItemComponentBuilder {

    @Override
    public MachineComponentType<ItemMachineComponent> type() {
        return Registration.ITEM_UPGRADE_MACHINE_COMPONENT.get();
    }

    @Override
    public PopupScreen makePopup(MachineEditScreen parent, @Nullable ItemMachineComponent.Template template, Consumer<Template> onFinish) {
        return new ItemUpgradeComponentBuilderPopup(parent, template, onFinish);
    }

    public static class ItemUpgradeComponentBuilderPopup extends ItemComponentBuilderPopup {

        public ItemUpgradeComponentBuilderPopup(BaseScreen parent, @Nullable Template template, Consumer<Template> onFinish) {
            super(parent, template, onFinish);
        }

        @Override
        public Template makeTemplate() {
            return new UpgradeItemMachineComponent.Template(this.id.getValue(), this.mode.getValue(), this.capacity.intValue(), Optional.of(this.maxInput.intValue()), Optional.of(this.maxOutput.intValue()), Collections.emptyList(), false, Optional.of(this.mode.getValue().getBaseConfig()), this.locked.selected());
        }
    }
}

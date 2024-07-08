package fr.frinn.custommachinery.client.screen.creation.component.builder;

import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.utils.Filter;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.common.component.item.FluidHandlerItemMachineComponent;
import fr.frinn.custommachinery.common.component.item.ItemMachineComponent;
import fr.frinn.custommachinery.common.component.item.ItemMachineComponent.Template;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ItemFluidComponentBuilder extends ItemComponentBuilder {

    @Override
    public MachineComponentType<ItemMachineComponent> type() {
        return Registration.ITEM_FLUID_MACHINE_COMPONENT.get();
    }

    @Override
    public PopupScreen makePopup(MachineEditScreen parent, @Nullable Template template, Consumer<Template> onFinish) {
        return new ItemFluidComponentBuilderPopup(parent, template, onFinish);
    }

    public static class ItemFluidComponentBuilderPopup extends ItemComponentBuilderPopup {

        private EditBox tanks;

        public ItemFluidComponentBuilderPopup(BaseScreen parent, @Nullable ItemMachineComponent.Template template, Consumer<Template> onFinish) {
            super(parent, template, onFinish);
        }

        @Override
        public Template makeTemplate() {
            return new FluidHandlerItemMachineComponent.Template(this.id.getValue(), this.mode.getValue(), this.capacity.intValue(), Optional.of(this.maxInput.intValue()), Optional.of(this.maxOutput.intValue()), this.baseTemplate().map(template -> template.filter).orElse(Filter.empty()), Optional.of(this.mode.getValue().getBaseConfig()), this.locked.selected(), this.getTanks());
        }

        @Override
        protected void init() {
            super.init();
            this.tanks = this.propertyList.add(Component.translatable("custommachinery.gui.creation.components.item.tanks"), new EditBox(this.font, 0, 0, 100, 20, Component.translatable("custommachinery.gui.creation.components.item.tanks")));
            this.baseTemplate().ifPresent(template -> {
                if(template instanceof FluidHandlerItemMachineComponent.Template t)
                    this.setTanks(t.tanks);
            });
            this.tanks.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.components.item.tanks.tooltip")));
        }

        private List<String> getTanks() {
            return Arrays.stream(this.tanks.getValue().split(",")).filter(s -> !s.isEmpty()).toList();
        }

        private void setTanks(List<String> tanks) {
            StringBuilder builder = new StringBuilder();
            Iterator<String> iterator = tanks.iterator();
            while(iterator.hasNext()) {
                builder.append(iterator.next());
                if(iterator.hasNext())
                    builder.append(",");
            }
            this.tanks.setValue(builder.toString());
        }
    }
}

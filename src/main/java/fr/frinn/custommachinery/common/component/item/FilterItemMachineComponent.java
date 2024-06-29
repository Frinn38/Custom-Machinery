package fr.frinn.custommachinery.common.component.item;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import fr.frinn.custommachinery.common.util.slot.FilterSlotItemComponent;
import fr.frinn.custommachinery.common.util.slot.SlotItemComponent;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Optional;

public class FilterItemMachineComponent extends ItemMachineComponent {

    public FilterItemMachineComponent(IMachineComponentManager manager, ComponentIOMode mode, String id, int capacity, int maxInput, int maxOutput, List<IIngredient<Item>> filter, boolean whitelist, SideConfig.Template config, boolean locked) {
        super(manager, ComponentIOMode.NONE, id, capacity, maxInput, maxOutput, filter, whitelist, SideConfig.Template.DEFAULT_ALL_NONE_DISABLED, locked);
    }

    @Override
    public MachineComponentType<ItemMachineComponent> getType() {
        return Registration.ITEM_FILTER_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean isLocked() {
        return true;
    }

    @Override
    public SlotItemComponent makeSlot(int index, int x, int y) {
        return new FilterSlotItemComponent(this, index, x, y);
    }

    @Override
    public boolean shouldDrop() {
        return false;
    }

    public static class Template extends ItemMachineComponent.Template {

        public static final NamedCodec<Template> CODEC = defaultCodec(Template::new, "Filter item machine component");

        public Template(String id, ComponentIOMode mode, int capacity, Optional<Integer> maxInput, Optional<Integer> maxOutput, List<IIngredient<Item>> filter, boolean whitelist, Optional<SideConfig.Template> config, boolean locked) {
            super(id, mode, capacity, maxInput, maxOutput, filter, whitelist, config, locked);
        }

        @Override
        public MachineComponentType<ItemMachineComponent> getType() {
            return Registration.ITEM_FILTER_MACHINE_COMPONENT.get();
        }

        @Override
        public ItemMachineComponent build(IMachineComponentManager manager) {
            return new FilterItemMachineComponent(manager, this.mode, this.id, this.capacity, this.maxInput, this.maxOutput, this.filter, this.whitelist, this.config, this.locked);
        }
    }
}

package fr.frinn.custommachinery.common.component.item;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.utils.Filter;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.slot.ResultSlotItemComponent;
import fr.frinn.custommachinery.common.util.slot.SlotItemComponent;
import fr.frinn.custommachinery.impl.component.config.IOSideConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class ResultItemMachineComponent extends ItemMachineComponent {

    public ResultItemMachineComponent(IMachineComponentManager manager, ComponentIOMode mode, String id, int capacity, int maxInput, int maxOutput, Filter<Item> filter, IOSideConfig.Template configTemplate, boolean locked) {
        super(manager, mode, id, capacity, maxInput, maxOutput, filter, configTemplate, locked);
    }

    @Override
    public MachineComponentType<ItemMachineComponent> getType() {
        return Registration.ITEM_RESULT_MACHINE_COMPONENT.get();
    }

    @Override
    public SlotItemComponent makeSlot(int index, int x, int y) {
        return new ResultSlotItemComponent(this, index, x, y);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return false;
    }

    @Override
    public boolean canOutput() {
        return false;
    }

    @Override
    public boolean shouldDrop() {
        return false;
    }

    public static class Template extends ItemMachineComponent.Template {

        public static final NamedCodec<Template> CODEC = defaultCodec(Template::new, "Result item component");

        public Template(String id, ComponentIOMode mode, int capacity, Optional<Integer> maxInput, Optional<Integer> maxOutput, Filter<Item> filter, Optional<IOSideConfig.Template> config, boolean locked) {
            super(id, mode, capacity, maxInput, maxOutput, filter, config, locked);
        }

        @Override
        public MachineComponentType<ItemMachineComponent> getType() {
            return Registration.ITEM_RESULT_MACHINE_COMPONENT.get();
        }

        @Override
        public boolean canAccept(Object ingredient, boolean isInput, IMachineComponentManager manager) {
            return false;
        }

        @Override
        public ItemMachineComponent build(IMachineComponentManager manager) {
            return new ResultItemMachineComponent(manager, this.mode, this.id, this.capacity, this.maxInput, this.maxOutput, this.filter, this.config, this.locked);
        }
    }
}

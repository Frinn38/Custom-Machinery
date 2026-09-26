package fr.frinn.custommachinery.common.component.item;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.ITickableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.common.util.Filter;
import fr.frinn.custommachinery.impl.component.config.IOSideConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class EnergyItemMachineComponent extends ItemMachineComponent implements ITickableComponent {

    public EnergyItemMachineComponent(IMachineComponentManager manager, ComponentIOMode mode, String id, int capacity, int maxInput, int maxOutput, Filter<Item> filter, IOSideConfig.Template configTemplate, boolean locked) {
        super(manager, mode, id, capacity, maxInput, maxOutput, filter, configTemplate, locked);
    }

    @Override
    public MachineComponentType<ItemMachineComponent> getType() {
        return CMRegistration.ITEM_ENERGY_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return super.isValid(index, resource) && resource.toStack().getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(resource.toStack())) != null;
    }

    @Override
    public void serverTick() {
        ItemStack stack = this.getItemStack();
        if(stack.isEmpty() || stack.getCapability(Capabilities.Energy.ITEM, ItemAccess.forHandlerIndexStrict(this, 0)) == null || this.getManager().getComponent(CMRegistration.ENERGY_MACHINE_COMPONENT.get()).isEmpty())
            return;

        EnergyMachineComponent buffer = this.getManager().getComponent(CMRegistration.ENERGY_MACHINE_COMPONENT.get()).get();

        if(this.getMode().isInput())
            fillBufferFromStack(buffer, this);
        else if(this.getMode().isOutput())
            fillStackFromBuffer(this, buffer);
    }

    public static void fillBufferFromStack(EnergyMachineComponent buffer, ItemMachineComponent slot) {
        ItemStack stack = slot.getItemStack();
        if(stack.isEmpty())
            return;

        EnergyHandler handler =  stack.getCapability(Capabilities.Energy.ITEM, ItemAccess.forHandlerIndexStrict(slot, 0));
        EnergyHandlerUtil.move(handler, buffer, Integer.MAX_VALUE, null);
    }

    public static void fillStackFromBuffer(ItemMachineComponent slot, EnergyMachineComponent buffer) {
        ItemStack stack = slot.getItemStack();
        if(stack.isEmpty())
            return;

        EnergyHandler handler =  stack.getCapability(Capabilities.Energy.ITEM, ItemAccess.forHandlerIndexStrict(slot, 0));
        EnergyHandlerUtil.move(buffer, handler, Integer.MAX_VALUE, null);
    }

    public static class Template extends ItemMachineComponent.Template {

        public static final NamedCodec<Template> CODEC = defaultCodec(Template::new, "Energy item machine component");

        public Template(String id, ComponentIOMode mode, int capacity, int maxInput, int maxOutput, Filter<Item> filter, IOSideConfig.Template config, boolean locked) {
            super(id, mode, capacity, maxInput, maxOutput, filter, config, locked);
        }

        @Override
        public MachineComponentType<ItemMachineComponent> getType() {
            return CMRegistration.ITEM_ENERGY_MACHINE_COMPONENT.get();
        }

        @Override
        public boolean isItemValid(IMachineComponentManager manager, ItemStack stack) {
            return stack.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(stack)) != null;
        }

        @Override
        public ItemMachineComponent build(IMachineComponentManager manager) {
            return new EnergyItemMachineComponent(manager, this.mode, this.id, this.capacity, this.maxInput, this.maxOutput, this.filter, this.config, this.locked);
        }
    }
}

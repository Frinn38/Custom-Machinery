package fr.frinn.custommachinery.common.component.item;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.ITickableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.utils.Filter;
import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.List;
import java.util.Optional;

public class EnergyItemMachineComponent extends ItemMachineComponent implements ITickableComponent {

    public EnergyItemMachineComponent(IMachineComponentManager manager, ComponentIOMode mode, String id, int capacity, int maxInput, int maxOutput, Filter<Item> filter, SideConfig.Template configTemplate, boolean locked) {
        super(manager, mode, id, capacity, maxInput, maxOutput, filter, configTemplate, locked);
    }

    @Override
    public MachineComponentType<ItemMachineComponent> getType() {
        return Registration.ITEM_ENERGY_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return super.isItemValid(slot, stack) && stack.getCapability(EnergyStorage.ITEM) != null;
    }

    @Override
    public void serverTick() {
        ItemStack stack = this.getItemStack();
        if(stack.isEmpty() || stack.getCapability(EnergyStorage.ITEM) == null || this.getManager().getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).isEmpty())
            return;

        EnergyMachineComponent buffer = this.getManager().getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).get();

        if(this.getMode().isInput())
            fillBufferFromStack(buffer, this);
        else if(this.getMode().isOutput())
            fillStackFromBuffer(this, buffer);
    }

    public static void fillBufferFromStack(EnergyMachineComponent buffer, ItemMachineComponent slot) {
        ItemStack stack = slot.getItemStack();
        if(stack.isEmpty())
            return;

        IEnergyStorage handler =  stack.getCapability(EnergyStorage.ITEM);
        if(handler == null)
            return;

        if(!handler.canExtract())
            return;

        int maxExtract = handler.extractEnergy(Integer.MAX_VALUE, true);

        if(maxExtract <= 0)
            return;

        long maxInsert = buffer.receiveEnergy(maxExtract, true);

        if(maxInsert <= 0)
            return;

        int extracted = handler.extractEnergy(Utils.toInt(maxInsert), false);

        if(extracted > 0)
            buffer.receiveEnergy(extracted, false);
    }

    public static void fillStackFromBuffer(ItemMachineComponent slot, EnergyMachineComponent buffer) {
        ItemStack stack = slot.getItemStack();
        if(stack.isEmpty())
            return;

        IEnergyStorage handler =  stack.getCapability(EnergyStorage.ITEM);
        if(handler == null)
            return;

        if(!handler.canReceive())
            return;

        long maxExtract = buffer.extractEnergy(Integer.MAX_VALUE, true);

        if(maxExtract <= 0)
            return;

        int maxInsert = handler.receiveEnergy(Utils.toInt(maxExtract), true);

        if(maxInsert <= 0)
            return;

        long extracted = buffer.extractEnergy(maxInsert, false);

        if(extracted > 0)
            handler.receiveEnergy(Utils.toInt(extracted), false);
    }

    public static class Template extends ItemMachineComponent.Template {

        public static final NamedCodec<Template> CODEC = defaultCodec(Template::new, "Energy item machine component");

        public Template(String id, ComponentIOMode mode, int capacity, Optional<Integer> maxInput, Optional<Integer> maxOutput, Filter<Item> filter, Optional<SideConfig.Template> config, boolean locked) {
            super(id, mode, capacity, maxInput, maxOutput, filter, config, locked);
        }

        @Override
        public MachineComponentType<ItemMachineComponent> getType() {
            return Registration.ITEM_ENERGY_MACHINE_COMPONENT.get();
        }

        @Override
        public boolean isItemValid(IMachineComponentManager manager, ItemStack stack) {
            return stack.getCapability(EnergyStorage.ITEM) != null;
        }

        @Override
        public ItemMachineComponent build(IMachineComponentManager manager) {
            return new EnergyItemMachineComponent(manager, this.mode, this.id, this.capacity, this.maxInput, this.maxOutput, this.filter, this.config, this.locked);
        }
    }
}

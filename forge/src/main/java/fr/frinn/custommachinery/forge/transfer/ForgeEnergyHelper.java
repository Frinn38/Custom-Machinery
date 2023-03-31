package fr.frinn.custommachinery.forge.transfer;

import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.common.util.transfer.IEnergyHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.registries.ForgeRegistries;

public class ForgeEnergyHelper implements IEnergyHelper {

    private static final Component ENERGY_UNIT = new TranslatableComponent("unit.energy.forge");

    @Override
    public Component unit() {
        return ENERGY_UNIT;
    }

    @Override
    public boolean isEnergyHandler(ItemStack stack) {
        return stack.getCapability(CapabilityEnergy.ENERGY).isPresent();
    }

    @Override
    public void fillBufferFromStack(EnergyMachineComponent buffer, ItemMachineComponent slot) {
        ItemStack stack = slot.getItemStack();
        if(stack.isEmpty())
            return;
        IEnergyStorage handler =  stack.getCapability(CapabilityEnergy.ENERGY).orElseThrow(() -> new IllegalStateException("Can't fill energy buffer from non energy storage item: " + ForgeRegistries.ITEMS.getKey(stack.getItem())));
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

    @Override
    public void fillStackFromBuffer(ItemMachineComponent slot, EnergyMachineComponent buffer) {
        ItemStack stack = slot.getItemStack();
        if(stack.isEmpty())
            return;
        IEnergyStorage handler =  stack.getCapability(CapabilityEnergy.ENERGY).orElseThrow(() -> new IllegalStateException("Can't fill energy buffer from non energy storage item: " + ForgeRegistries.ITEMS.getKey(stack.getItem())));
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
}

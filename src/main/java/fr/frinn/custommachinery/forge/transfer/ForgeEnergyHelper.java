package fr.frinn.custommachinery.forge.transfer;

import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class ForgeEnergyHelper {

    private static final Component ENERGY_UNIT = Component.translatable("unit.energy.forge");

    public static Component unit() {
        return ENERGY_UNIT;
    }

    public static boolean isEnergyHandler(ItemStack stack) {
        return stack.getCapability(EnergyStorage.ITEM) != null;
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
}

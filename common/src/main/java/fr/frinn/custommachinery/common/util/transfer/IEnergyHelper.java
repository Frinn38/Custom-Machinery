package fr.frinn.custommachinery.common.util.transfer;

import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public interface IEnergyHelper {

    Component unit();

    boolean isEnergyHandler(ItemStack stack);

    void fillBufferFromStack(EnergyMachineComponent buffer, ItemMachineComponent slot);

    void fillStackFromBuffer(ItemMachineComponent slot, EnergyMachineComponent buffer);
}

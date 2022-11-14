package fr.frinn.custommachinery.fabric.transfer;

import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.util.transfer.IEnergyHelper;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.minecraft.world.item.ItemStack;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;

@SuppressWarnings("UnstableApiUsage")
public class FabricEnergyHelper implements IEnergyHelper {

    @Override
    public String unit() {
        return "E";
    }

    @Override
    public boolean isEnergyHandler(ItemStack stack) {
        return EnergyStorageUtil.isEnergyStorage(stack);
    }

    @Override
    public void fillBufferFromStack(EnergyMachineComponent buffer, ItemMachineComponent slot) {
        ItemStack stack = slot.getItemStack();
        if(stack.isEmpty())
            return;
        EnergyStorage itemStorage = EnergyStorage.ITEM.find(stack, ContainerItemContext.ofSingleSlot(new ItemSlot(slot, null)));

        EnergyStorageUtil.move(itemStorage, new EnergyBuffer(buffer, null), Long.MAX_VALUE, null);
    }

    @Override
    public void fillStackFromBuffer(ItemMachineComponent slot, EnergyMachineComponent buffer) {
        ItemStack stack = slot.getItemStack();
        if(stack.isEmpty())
            return;
        EnergyStorage itemStorage = EnergyStorage.ITEM.find(stack, ContainerItemContext.ofSingleSlot(new ItemSlot(slot, null)));

        EnergyStorageUtil.move(new EnergyBuffer(buffer, null), itemStorage, Long.MAX_VALUE, null);
    }
}

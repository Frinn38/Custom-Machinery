package fr.frinn.custommachinery.common.component.variant.item;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.variant.ITickableComponentVariant;
import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.component.variant.ItemComponentVariant;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class EnergyItemComponentVariant extends ItemComponentVariant implements ITickableComponentVariant<ItemMachineComponent> {

    public static final EnergyItemComponentVariant INSTANCE = new EnergyItemComponentVariant();
    public static final NamedCodec<EnergyItemComponentVariant> CODEC = NamedCodec.unit(INSTANCE, "Energy item component");
    public static final ResourceLocation ID = CustomMachinery.rl("energy");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public NamedCodec<EnergyItemComponentVariant> getCodec() {
        return CODEC;
    }

    @Override
    public boolean canAccept(IMachineComponentManager manager, ItemStack stack) {
        return stack.getCapability(EnergyStorage.ITEM) != null;
    }

    @Override
    public void tick(ItemMachineComponent slot) {
        ItemStack stack = slot.getItemStack();
        if(stack.isEmpty() || stack.getCapability(EnergyStorage.ITEM) == null || slot.getManager().getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).isEmpty())
            return;

        EnergyMachineComponent buffer = slot.getManager().getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).get();

        if(slot.getMode().isInput())
            fillBufferFromStack(buffer, slot);
        else if(slot.getMode().isOutput())
            fillStackFromBuffer(slot, buffer);
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

package fr.frinn.custommachinery.common.component.variant.item;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.variant.ITickableComponentVariant;
import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.forge.transfer.ForgeEnergyHelper;
import fr.frinn.custommachinery.impl.component.variant.ItemComponentVariant;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;

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
        if(stack.isEmpty() || !ForgeEnergyHelper.isEnergyHandler(stack) || slot.getManager().getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).isEmpty())
            return;

        EnergyMachineComponent buffer = slot.getManager().getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).get();

        if(slot.getMode().isInput())
            ForgeEnergyHelper.fillBufferFromStack(buffer, slot);
        else if(slot.getMode().isOutput())
            ForgeEnergyHelper.fillStackFromBuffer(slot, buffer);
    }
}

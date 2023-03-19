package fr.frinn.custommachinery.common.component.variant.item;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.PlatformHelper;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.variant.ITickableComponentVariant;
import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.transfer.IEnergyHelper;
import fr.frinn.custommachinery.impl.component.variant.ItemComponentVariant;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class EnergyItemComponentVariant extends ItemComponentVariant implements ITickableComponentVariant<ItemMachineComponent> {

    public static final EnergyItemComponentVariant INSTANCE = new EnergyItemComponentVariant();
    public static final NamedCodec<EnergyItemComponentVariant> CODEC = NamedCodec.unit(INSTANCE, "Energy item component");
    public static final ResourceLocation ID = new ResourceLocation(CustomMachinery.MODID, "energy");

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
        return PlatformHelper.energy().isEnergyHandler(stack);
    }

    @Override
    public void tick(ItemMachineComponent slot) {
        ItemStack stack = slot.getItemStack();
        IEnergyHelper helper = PlatformHelper.energy();
        if(stack.isEmpty() || !helper.isEnergyHandler(stack) || slot.getManager().getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).isEmpty())
            return;

        EnergyMachineComponent buffer = slot.getManager().getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).get();

        if(slot.getMode().isInput())
            helper.fillBufferFromStack(buffer, slot);
        else if(slot.getMode().isOutput())
            helper.fillStackFromBuffer(slot, buffer);
    }
}

package fr.frinn.custommachinery.impl.component.variant;

import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.variant.IComponentVariant;
import net.minecraft.world.item.ItemStack;

public abstract class ItemComponentVariant implements IComponentVariant {

    public abstract boolean canAccept(IMachineComponentManager manager, ItemStack stack);

    public boolean canOutput(IMachineComponentManager manager) {
        return true;
    }

    public boolean shouldDrop(IMachineComponentManager manager) {
        return true;
    }
}

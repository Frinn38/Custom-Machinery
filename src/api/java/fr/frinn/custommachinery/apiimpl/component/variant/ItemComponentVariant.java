package fr.frinn.custommachinery.apiimpl.component.variant;

import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.variant.IComponentVariant;
import net.minecraft.world.item.ItemStack;

public abstract class ItemComponentVariant implements IComponentVariant {

    public abstract boolean isItemValid(IMachineComponentManager manager, ItemStack stack);
}

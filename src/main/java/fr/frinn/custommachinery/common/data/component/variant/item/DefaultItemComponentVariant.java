package fr.frinn.custommachinery.common.data.component.variant.item;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.impl.component.variant.ItemComponentVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class DefaultItemComponentVariant extends ItemComponentVariant {

    public static final DefaultItemComponentVariant INSTANCE = new DefaultItemComponentVariant();
    private static final ResourceLocation ID = new ResourceLocation(CustomMachinery.MODID, "default");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void tick(IMachineComponentManager manager) {

    }

    @Override
    public boolean isItemValid(IMachineComponentManager manager, ItemStack stack) {
        return true;
    }
}

package fr.frinn.custommachinery.common.component.variant.item;

import dev.architectury.registry.fuel.FuelRegistry;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.apiimpl.component.variant.ItemComponentVariant;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class FuelItemComponentVariant extends ItemComponentVariant {

    public static final FuelItemComponentVariant INSTANCE = new FuelItemComponentVariant();
    private static final ResourceLocation ID = new ResourceLocation(CustomMachinery.MODID, "fuel");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public boolean isItemValid(IMachineComponentManager manager, ItemStack stack) {
        return FuelRegistry.get(stack) > 0;
    }
}

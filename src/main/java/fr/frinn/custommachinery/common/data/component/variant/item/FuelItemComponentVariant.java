package fr.frinn.custommachinery.common.data.component.variant.item;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.impl.component.variant.ItemComponentVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeHooks;

public class FuelItemComponentVariant extends ItemComponentVariant {

    public static final FuelItemComponentVariant INSTANCE = new FuelItemComponentVariant();
    private static final ResourceLocation ID = new ResourceLocation(CustomMachinery.MODID, "fuel");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void tick(IMachineComponentManager manager) {

    }

    @Override
    public boolean isItemValid(IMachineComponentManager manager, ItemStack stack) {
        return ForgeHooks.getBurnTime(stack, IRecipeType.SMELTING) > 0;
    }
}

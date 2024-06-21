package fr.frinn.custommachinery.common.component.variant.item;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.impl.component.variant.ItemComponentVariant;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

public class FuelItemComponentVariant extends ItemComponentVariant {

    public static final FuelItemComponentVariant INSTANCE = new FuelItemComponentVariant();
    public static final NamedCodec<FuelItemComponentVariant> CODEC = NamedCodec.unit(INSTANCE);
    public static final ResourceLocation ID = CustomMachinery.rl("fuel");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public NamedCodec<FuelItemComponentVariant> getCodec() {
        return CODEC;
    }

    @Override
    public boolean canAccept(IMachineComponentManager manager, ItemStack stack) {
        return stack.getBurnTime(RecipeType.SMELTING) > 0;
    }
}

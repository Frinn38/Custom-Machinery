package fr.frinn.custommachinery.common.component.variant.item;

import com.mojang.serialization.Codec;
import dev.architectury.registry.fuel.FuelRegistry;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.impl.component.variant.ItemComponentVariant;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class FuelItemComponentVariant extends ItemComponentVariant {

    public static final FuelItemComponentVariant INSTANCE = new FuelItemComponentVariant();
    public static final Codec<FuelItemComponentVariant> CODEC = Codec.unit(INSTANCE);
    public static final ResourceLocation ID = new ResourceLocation(CustomMachinery.MODID, "fuel");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Codec<FuelItemComponentVariant> getCodec() {
        return CODEC;
    }

    @Override
    public boolean canAccept(IMachineComponentManager manager, ItemStack stack) {
        return FuelRegistry.get(stack) > 0;
    }
}

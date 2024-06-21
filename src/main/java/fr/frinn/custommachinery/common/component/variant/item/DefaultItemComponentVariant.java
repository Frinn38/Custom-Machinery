package fr.frinn.custommachinery.common.component.variant.item;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.impl.component.variant.ItemComponentVariant;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class DefaultItemComponentVariant extends ItemComponentVariant {

    public static final DefaultItemComponentVariant INSTANCE = new DefaultItemComponentVariant();
    public static final NamedCodec<DefaultItemComponentVariant> CODEC = NamedCodec.unit(INSTANCE);
    public static final ResourceLocation ID = CustomMachinery.rl("default");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public NamedCodec<DefaultItemComponentVariant> getCodec() {
        return CODEC;
    }

    @Override
    public boolean canAccept(IMachineComponentManager manager, ItemStack stack) {
        return true;
    }
}

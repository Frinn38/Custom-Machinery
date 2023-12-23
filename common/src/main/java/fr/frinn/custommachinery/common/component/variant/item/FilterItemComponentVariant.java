package fr.frinn.custommachinery.common.component.variant.item;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.impl.component.variant.ItemComponentVariant;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class FilterItemComponentVariant extends ItemComponentVariant {

    public static final FilterItemComponentVariant INSTANCE = new FilterItemComponentVariant();
    public static final NamedCodec<FilterItemComponentVariant> CODEC = NamedCodec.unit(INSTANCE);
    public static final ResourceLocation ID = new ResourceLocation(CustomMachinery.MODID, "filter");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public NamedCodec<FilterItemComponentVariant> getCodec() {
        return CODEC;
    }

    @Override
    public boolean canAccept(IMachineComponentManager manager, ItemStack stack) {
        return true;
    }

    @Override
    public boolean canOutput(IMachineComponentManager manager) {
        return true;
    }
}

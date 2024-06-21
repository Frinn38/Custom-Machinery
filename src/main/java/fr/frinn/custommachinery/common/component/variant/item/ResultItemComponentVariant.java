package fr.frinn.custommachinery.common.component.variant.item;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.impl.component.variant.ItemComponentVariant;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ResultItemComponentVariant extends ItemComponentVariant {

    public static final ResultItemComponentVariant INSTANCE = new ResultItemComponentVariant();
    public static final NamedCodec<ResultItemComponentVariant> CODEC = NamedCodec.unit(INSTANCE);
    public static final ResourceLocation ID = CustomMachinery.rl("result");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public NamedCodec<ResultItemComponentVariant> getCodec() {
        return CODEC;
    }

    @Override
    public boolean canAccept(IMachineComponentManager manager, ItemStack stack) {
        return false;
    }

    @Override
    public boolean canOutput(IMachineComponentManager manager) {
        return false;
    }

    @Override
    public boolean shouldDrop(IMachineComponentManager manager) {
        return false;
    }
}

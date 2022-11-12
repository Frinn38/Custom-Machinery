package fr.frinn.custommachinery.common.component.variant.item;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.impl.component.variant.ItemComponentVariant;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ResultItemComponentVariant extends ItemComponentVariant {

    public static final ResultItemComponentVariant INSTANCE = new ResultItemComponentVariant();
    public static final Codec<ResultItemComponentVariant> CODEC = Codec.unit(INSTANCE);
    public static final ResourceLocation ID = new ResourceLocation(CustomMachinery.MODID, "result");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Codec<ResultItemComponentVariant> getCodec() {
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
}

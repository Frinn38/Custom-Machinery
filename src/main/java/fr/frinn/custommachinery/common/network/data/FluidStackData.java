package fr.frinn.custommachinery.common.network.data;

import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.network.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidStackData extends Data<FluidStack> {

    public FluidStackData(short id, FluidStack value) {
        super(Registration.FLUIDSTACK_DATA.get(), id, value);
    }

    public FluidStackData(short id, RegistryFriendlyByteBuf buffer) {
        this(id, buffer.readJsonWithCodec(FluidStack.OPTIONAL_CODEC));
    }

    @Override
    public void writeData(RegistryFriendlyByteBuf buffer) {
        super.writeData(buffer);
        buffer.writeJsonWithCodec(FluidStack.OPTIONAL_CODEC, getValue());
    }
}

package fr.frinn.custommachinery.common.network.data;

import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;

public class FluidStackData extends Data<FluidStack> {

    public FluidStackData(short id, FluidStack value) {
        super(Registration.FLUIDSTACK_DATA.get(), id, value);
    }

    public FluidStackData(short id, FriendlyByteBuf buffer) {
        this(id, buffer.readFluidStack());
    }

    @Override
    public void writeData(FriendlyByteBuf buffer) {
        super.writeData(buffer);
        buffer.writeFluidStack(getValue());
    }
}

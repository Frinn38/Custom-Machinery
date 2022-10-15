package fr.frinn.custommachinery.common.network.data;

import dev.architectury.fluid.FluidStack;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.network.FriendlyByteBuf;

public class FluidStackData extends Data<FluidStack> {

    public FluidStackData(short id, FluidStack value) {
        super(Registration.FLUIDSTACK_DATA.get(), id, value);
    }

    public FluidStackData(short id, FriendlyByteBuf buffer) {
        this(id, FluidStack.read(buffer));
    }

    @Override
    public void writeData(FriendlyByteBuf buffer) {
        super.writeData(buffer);
        getValue().write(buffer);
    }
}

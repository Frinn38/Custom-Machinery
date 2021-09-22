package fr.frinn.custommachinery.impl.network.data;

import fr.frinn.custommachinery.api.network.DataType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;

public class FluidStackData extends Data<FluidStack> {

    private FluidStack value;

    public FluidStackData(short id, FluidStack value) {
        super(DataType.FLUIDSTACK_DATA.get(), id);
        this.value = value;
    }

    public FluidStackData(short id, PacketBuffer buffer) {
        this(id, buffer.readFluidStack());
    }

    @Override
    public void writeData(PacketBuffer buffer) {
        super.writeData(buffer);
        buffer.writeFluidStack(this.value);
    }

    @Override
    public FluidStack getValue() {
        return this.value;
    }
}

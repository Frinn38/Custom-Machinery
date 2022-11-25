package fr.frinn.custommachinery.common.network.data;

import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.network.Data;
import net.minecraft.network.FriendlyByteBuf;

public class DoubleData extends Data<Double> {

    public DoubleData(short id, double value) {
        super(Registration.DOUBLE_DATA.get(), id, value);
    }

    public DoubleData(short id, FriendlyByteBuf buffer) {
        this(id, buffer.readDouble());
    }

    @Override
    public void writeData(FriendlyByteBuf buffer) {
        super.writeData(buffer);
        buffer.writeDouble(getValue());
    }
}

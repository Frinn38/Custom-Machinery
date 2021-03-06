package fr.frinn.custommachinery.common.network.sync.data;

import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.network.PacketBuffer;

public class DoubleData extends Data<Double> {

    private double value;

    public DoubleData(short id, double value) {
        super(Registration.DOUBLE_DATA.get(), id);
        this.value = value;
    }

    public DoubleData(short id, PacketBuffer buffer) {
        this(id, buffer.readDouble());
    }

    @Override
    public void writeData(PacketBuffer buffer) {
        super.writeData(buffer);
        buffer.writeDouble(this.value);
    }

    @Override
    public Double getValue() {
        return this.value;
    }
}

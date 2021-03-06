package fr.frinn.custommachinery.common.network.sync.data;

import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.network.PacketBuffer;

public class IntegerData extends Data<Integer> {

    private int value;

    public IntegerData(short id, int value) {
        super(Registration.INTEGER_DATA.get(), id);
        this.value = value;
    }

    public IntegerData(short id, PacketBuffer buffer) {
        this(id, buffer.readInt());
    }

    @Override
    public void writeData(PacketBuffer buffer) {
        super.writeData(buffer);
        buffer.writeInt(this.value);
    }

    @Override
    public Integer getValue() {
        return this.value;
    }
}

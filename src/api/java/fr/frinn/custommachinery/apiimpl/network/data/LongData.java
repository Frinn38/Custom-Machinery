package fr.frinn.custommachinery.apiimpl.network.data;

import fr.frinn.custommachinery.api.network.DataType;
import net.minecraft.network.PacketBuffer;

public class LongData extends Data<Long> {

    private final long value;

    public LongData(short id, long value) {
        super(DataType.LONG_DATA.get(), id);
        this.value = value;
    }

    public LongData(short id, PacketBuffer buffer) {
        this(id, buffer.readLong());
    }

    @Override
    public void writeData(PacketBuffer buffer) {
        super.writeData(buffer);
        buffer.writeLong(this.value);
    }

    @Override
    public Long getValue() {
        return this.value;
    }
}

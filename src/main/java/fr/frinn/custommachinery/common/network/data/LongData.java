package fr.frinn.custommachinery.common.network.data;

import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.network.FriendlyByteBuf;

public class LongData extends Data<Long> {

    private final long value;

    public LongData(short id, long value) {
        super(Registration.LONG_DATA.get(), id);
        this.value = value;
    }

    public LongData(short id, FriendlyByteBuf buffer) {
        this(id, buffer.readLong());
    }

    @Override
    public void writeData(FriendlyByteBuf buffer) {
        super.writeData(buffer);
        buffer.writeLong(this.value);
    }

    @Override
    public Long getValue() {
        return this.value;
    }
}

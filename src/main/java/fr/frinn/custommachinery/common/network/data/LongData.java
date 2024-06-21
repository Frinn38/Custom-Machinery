package fr.frinn.custommachinery.common.network.data;

import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.network.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class LongData extends Data<Long> {

    public LongData(short id, long value) {
        super(Registration.LONG_DATA.get(), id, value);
    }

    public LongData(short id, RegistryFriendlyByteBuf buffer) {
        this(id, buffer.readLong());
    }

    @Override
    public void writeData(RegistryFriendlyByteBuf buffer) {
        super.writeData(buffer);
        buffer.writeLong(getValue());
    }
}

package fr.frinn.custommachinery.common.network.data;

import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.network.FriendlyByteBuf;

public class StringData extends Data<String> {

    public StringData(short id, String value) {
        super(Registration.STRING_DATA.get(), id, value);
    }

    public StringData(short id, FriendlyByteBuf buffer) {
        this(id, buffer.readUtf());
    }

    @Override
    public void writeData(FriendlyByteBuf buffer) {
        super.writeData(buffer);
        buffer.writeUtf(getValue());
    }
}

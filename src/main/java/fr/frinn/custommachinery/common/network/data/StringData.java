package fr.frinn.custommachinery.common.network.data;

import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.network.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class StringData extends Data<String> {

    public StringData(short id, String value) {
        super(Registration.STRING_DATA.get(), id, value);
    }

    public StringData(short id, RegistryFriendlyByteBuf buffer) {
        this(id, buffer.readUtf());
    }

    @Override
    public void writeData(RegistryFriendlyByteBuf buffer) {
        super.writeData(buffer);
        buffer.writeUtf(getValue());
    }
}

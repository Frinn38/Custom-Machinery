package fr.frinn.custommachinery.common.network.data;

import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.network.FriendlyByteBuf;

public class StringData extends Data<String> {

    private final String value;

    public StringData(short id, String value) {
        super(Registration.STRING_DATA.get(), id);
        this.value = value;
    }

    public StringData(short id, FriendlyByteBuf buffer) {
        this(id, buffer.readUtf());
    }

    @Override
    public void writeData(FriendlyByteBuf buffer) {
        super.writeData(buffer);
        buffer.writeUtf(this.value);
    }

    @Override
    public String getValue() {
        return this.value;
    }
}

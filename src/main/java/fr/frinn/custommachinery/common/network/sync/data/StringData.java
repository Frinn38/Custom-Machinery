package fr.frinn.custommachinery.common.network.sync.data;

import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.network.PacketBuffer;

public class StringData extends Data<String> {

    private String value;

    public StringData(short id, String value) {
        super(Registration.STRING_DATA.get(), id);
        this.value = value;
    }

    public StringData(short id, PacketBuffer buffer) {
        this(id, buffer.readString());
    }

    @Override
    public void writeData(PacketBuffer buffer) {
        super.writeData(buffer);
        buffer.writeString(this.value);
    }

    @Override
    public String getValue() {
        return this.value;
    }
}

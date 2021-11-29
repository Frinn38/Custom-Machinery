package fr.frinn.custommachinery.apiimpl.network.data;

import fr.frinn.custommachinery.api.network.DataType;
import net.minecraft.network.PacketBuffer;

public class StringData extends Data<String> {

    private String value;

    public StringData(short id, String value) {
        super(DataType.STRING_DATA.get(), id);
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

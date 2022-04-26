package fr.frinn.custommachinery.apiimpl.network.data;

import fr.frinn.custommachinery.api.network.DataType;
import net.minecraft.network.PacketBuffer;

public class BooleanData extends Data<Boolean> {

    private final boolean value;

    public BooleanData(short id, boolean value) {
        super(DataType.BOOLEAN_DATA.get(), id);
        this.value = value;
    }

    public BooleanData(short id, PacketBuffer buffer) {
        this(id, buffer.readBoolean());
    }

    @Override
    public void writeData(PacketBuffer buffer) {
        super.writeData(buffer);
        buffer.writeBoolean(this.value);
    }

    @Override
    public Boolean getValue() {
        return this.value;
    }
}

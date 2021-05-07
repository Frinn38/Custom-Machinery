package fr.frinn.custommachinery.common.network.sync.data;

import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.network.PacketBuffer;

public class BooleanData extends Data<Boolean> {

    private boolean value;

    public BooleanData(short id, boolean value) {
        super(Registration.BOOLEAN_DATA.get(), id);
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

package fr.frinn.custommachinery.common.network.data;

import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.network.FriendlyByteBuf;

public class BooleanData extends Data<Boolean> {

    private final boolean value;

    public BooleanData(short id, boolean value) {
        super(Registration.BOOLEAN_DATA.get(), id);
        this.value = value;
    }

    public BooleanData(short id, FriendlyByteBuf buffer) {
        this(id, buffer.readBoolean());
    }

    @Override
    public void writeData(FriendlyByteBuf buffer) {
        super.writeData(buffer);
        buffer.writeBoolean(this.value);
    }

    @Override
    public Boolean getValue() {
        return this.value;
    }
}

package fr.frinn.custommachinery.common.network.data;

import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.network.FriendlyByteBuf;

public class IntegerData extends Data<Integer> {

    public IntegerData(short id, int value) {
        super(Registration.INTEGER_DATA.get(), id, value);
    }

    public IntegerData(short id, FriendlyByteBuf buffer) {
        this(id, buffer.readInt());
    }

    @Override
    public void writeData(FriendlyByteBuf buffer) {
        super.writeData(buffer);
        buffer.writeInt(getValue());
    }
}

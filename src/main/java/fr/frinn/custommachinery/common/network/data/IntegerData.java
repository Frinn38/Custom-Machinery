package fr.frinn.custommachinery.common.network.data;

import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.network.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class IntegerData extends Data<Integer> {

    public IntegerData(short id, int value) {
        super(Registration.INTEGER_DATA.get(), id, value);
    }

    public IntegerData(short id, RegistryFriendlyByteBuf buffer) {
        this(id, buffer.readInt());
    }

    @Override
    public void writeData(RegistryFriendlyByteBuf buffer) {
        super.writeData(buffer);
        buffer.writeInt(getValue());
    }
}

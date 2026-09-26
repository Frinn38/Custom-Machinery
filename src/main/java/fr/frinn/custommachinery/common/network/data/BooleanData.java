package fr.frinn.custommachinery.common.network.data;

import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.impl.network.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class BooleanData extends Data<Boolean> {

    public BooleanData(short id, boolean value) {
        super(CMRegistration.BOOLEAN_DATA.get(), id, value);
    }

    public BooleanData(short id, RegistryFriendlyByteBuf buffer) {
        this(id, buffer.readBoolean());
    }

    @Override
    public void writeData(RegistryFriendlyByteBuf buffer) {
        super.writeData(buffer);
        buffer.writeBoolean(getValue());
    }
}

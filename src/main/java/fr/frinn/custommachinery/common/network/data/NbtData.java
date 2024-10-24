package fr.frinn.custommachinery.common.network.data;

import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.network.Data;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class NbtData extends Data<CompoundTag> {

    public NbtData(short id, CompoundTag value) {
        super(Registration.NBT_DATA.get(), id, value);
    }

    public NbtData(short id, RegistryFriendlyByteBuf buffer) {
        this(id, buffer.readNbt());
    }

    @Override
    public void writeData(RegistryFriendlyByteBuf buffer) {
        super.writeData(buffer);
        buffer.writeNbt(getValue());
    }
}

package fr.frinn.custommachinery.common.network.data;

import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.impl.network.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class FloatData extends Data<Float> {
  public FloatData(short id, Float value) {
    super(CMRegistration.FLOAT_DATA.get(), id, value);
  }

  public FloatData(short id, RegistryFriendlyByteBuf buffer) {
    this(id, buffer.readFloat());
  }

  public void writeData(RegistryFriendlyByteBuf buffer) {
    super.writeData(buffer);
    buffer.writeFloat(getValue());
  }
}

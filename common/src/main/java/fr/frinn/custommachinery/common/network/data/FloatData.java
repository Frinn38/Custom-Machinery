package fr.frinn.custommachinery.common.network.data;

import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.network.Data;
import net.minecraft.network.FriendlyByteBuf;

public class FloatData extends Data<Float> {
  public FloatData(short id, Float value) {
    super(Registration.FLOAT_DATA.get(), id, value);
  }

  public FloatData(short id, FriendlyByteBuf buffer) {
    this(id, buffer.readFloat());
  }

  public void writeData(FriendlyByteBuf buffer) {
    super.writeData(buffer);
    buffer.writeFloat(getValue());
  }
}

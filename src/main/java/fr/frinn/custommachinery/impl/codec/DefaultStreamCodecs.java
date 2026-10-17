package fr.frinn.custommachinery.impl.codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;

public class DefaultStreamCodecs {

    public static <B extends ByteBuf, E extends Enum<E>> StreamCodec<B, E> forEnum(Class<E> clazz) {
        return new StreamCodec<>() {

            public E decode(B input) {
                int id = VarInt.read(input);
                return clazz.getEnumConstants()[id];
            }

            public void encode(B output, E value) {
                int id = value.ordinal();
                VarInt.write(output, id);
            }
        };
    }
}

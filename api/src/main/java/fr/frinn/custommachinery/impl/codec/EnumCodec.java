package fr.frinn.custommachinery.impl.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.util.Locale;

public class EnumCodec<E extends Enum<E>> implements Codec<E> {

    private final Class<E> enumClass;

    public static <E extends Enum<E>> EnumCodec<E> of(Class<E> enumClass) {
        return new EnumCodec<>(enumClass);
    }

    private EnumCodec(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> ops, T input) {
        return ops.getStringValue(input).flatMap(s -> {
            try {
                return DataResult.success(Pair.of(Enum.valueOf(this.enumClass, s.toUpperCase(Locale.ROOT)), input));
            } catch (IllegalArgumentException e) {
                return DataResult.error(String.format("Not a valid %s: %s%n%s", this.enumClass.getSimpleName(), s, e.getMessage()));
            }
        });
    }

    @Override
    public <T> DataResult<T> encode(E input, DynamicOps<T> ops, T prefix) {
        T string = ops.createString(input.toString());
        return ops.mergeToPrimitive(prefix, string);
    }

    @Override
    public String toString() {
        return this.enumClass.getSimpleName();
    }
}

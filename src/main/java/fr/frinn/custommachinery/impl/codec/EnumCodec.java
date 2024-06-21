package fr.frinn.custommachinery.impl.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import fr.frinn.custommachinery.api.codec.NamedCodec;

import java.util.Locale;

public class EnumCodec<E extends Enum<E>> implements NamedCodec<E> {

    private final Class<E> enumClass;
    private final String name;

    public static <E extends Enum<E>> EnumCodec<E> of(Class<E> enumClass) {
        return of(enumClass, enumClass.getSimpleName());
    }

    public static <E extends Enum<E>> EnumCodec<E> of(Class<E> enumClass, String name) {
        return new EnumCodec<>(enumClass, name);
    }

    private EnumCodec(Class<E> enumClass, String name) {
        this.enumClass = enumClass;
        this.name = name;
    }

    @Override
    public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> ops, T input) {
        return ops.getStringValue(input).flatMap(s -> {
            try {
                return DataResult.success(Pair.of(Enum.valueOf(this.enumClass, s.toUpperCase(Locale.ROOT)), input));
            } catch (IllegalArgumentException e) {
                return DataResult.error(() -> String.format("Not a valid %s: %s%n%s", this.enumClass.getSimpleName(), s, e.getMessage()));
            }
        });
    }

    @Override
    public <T> DataResult<T> encode(DynamicOps<T> ops, E input, T prefix) {
        T string = ops.createString(input.toString());
        return ops.mergeToPrimitive(prefix, string);
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}

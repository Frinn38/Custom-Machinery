package fr.frinn.custommachinery.impl.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import fr.frinn.custommachinery.api.codec.NamedCodec;

public abstract class NumberCodec<A extends Number> implements NamedCodec<A> {

    public abstract <T> DataResult<A> parse(DynamicOps<T> ops, T input);

    public abstract A fromString(String s) throws NumberFormatException;

    @Override
    public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
        DataResult<A> result = parse(ops, input);
        if(result.result().isPresent())
            return result.map(n -> Pair.of(n, ops.empty()));
        DataResult<String> stringResult = ops.getStringValue(input);
        if(stringResult.result().isPresent()) {
            String s = stringResult.result().get();
            try {
                return DataResult.success(Pair.of(fromString(s), ops.empty()));
            } catch (NumberFormatException ignored) {}
        }
        return result.map(n -> Pair.of(n, input));
    }

    @Override
    public <T> DataResult<T> encode(DynamicOps<T> ops, A input, T prefix) {
        return ops.mergeToPrimitive(prefix, ops.createNumeric(input));
    }
}

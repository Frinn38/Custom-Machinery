package fr.frinn.custommachinery.api.utils;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import fr.frinn.custommachinery.api.CustomMachineryAPI;

import java.util.Objects;

/**
 * A copy of EitherCodec that also show both errors when failing to decode.
 */
public class EnhancedEitherCodec<F, S> implements Codec<Either<F, S>> {

    private final String error = "Can't deserialize %s using either %s or %s.%n%s%n%s";
    private final Codec<F> first;
    private final Codec<S> second;
    private final String name;

    public EnhancedEitherCodec(final Codec<F> first, final Codec<S> second, String name) {
        this.first = first;
        this.second = second;
        this.name = name;
    }

    @Override
    public <T> DataResult<Pair<Either<F, S>, T>> decode(final DynamicOps<T> ops, final T input) {
        final DataResult<Pair<Either<F, S>, T>> firstRead = first.decode(ops, input).map(vo -> vo.mapFirst(Either::left));
        if (firstRead.result().isPresent())
            return firstRead;
        String firstError = firstRead.error().map(DataResult.PartialResult::message).orElse("");
        if(CMLogger.shouldLogFirstEitherError())
            CustomMachineryAPI.warn("Can't deserialize %s with %s, trying with %s now.%n%s", this, first, second, firstError);
        return second.decode(ops, input).mapError(s -> String.format(error, this, first, second, firstError, s)).map(vo -> vo.mapFirst(Either::right));
    }

    @Override
    public <T> DataResult<T> encode(final Either<F, S> input, final DynamicOps<T> ops, final T prefix) {
        return input.map(
                value1 -> first.encode(value1, ops, prefix),
                value2 -> second.encode(value2, ops, prefix)
        );
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final EnhancedEitherCodec<?, ?> eitherCodec = (EnhancedEitherCodec<?, ?>) o;
        return Objects.equals(first, eitherCodec.first) && Objects.equals(second, eitherCodec.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return name;
    }
}

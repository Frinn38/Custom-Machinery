package fr.frinn.custommachinery.impl.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.api.codec.NamedCodec;

import java.util.Objects;

public class EnhancedEitherCodec<F, S> implements NamedCodec<Either<F, S>> {

    private final String error = "Can't deserialize %s using either %s or %s.%n%s%n%s";
    private final NamedCodec<F> first;
    private final NamedCodec<S> second;
    private final String name;

    public static <F, S> EnhancedEitherCodec<F, S> of(NamedCodec<F> first, NamedCodec<S> second) {
        return of(first, second, "Either<" + first.name() + ", " + second.name() + ">");
    }

    public static <F, S> EnhancedEitherCodec<F, S> of(NamedCodec<F> first, NamedCodec<S> second, String name) {
        return new EnhancedEitherCodec<>(first, second, name);
    }

    private EnhancedEitherCodec(final NamedCodec<F> first, final NamedCodec<S> second, String name) {
        this.first = first;
        this.second = second;
        this.name = name;
    }

    @Override
    public <T> DataResult<Pair<Either<F, S>, T>> decode(final DynamicOps<T> ops, final T input) {
        final DataResult<Pair<Either<F, S>, T>> firstRead = this.first.decode(ops, input).map(vo -> vo.mapFirst(Either::left));
        if (firstRead.result().isPresent())
            return firstRead;
        String firstError = firstRead.error().map(DataResult.PartialResult::message).orElse("");
        if(ICustomMachineryAPI.INSTANCE.config().logFirstEitherError())
            ICustomMachineryAPI.INSTANCE.logger().warn("Can't deserialize {} with {}, trying with {} now.\n{}", this, first, second, firstError);
        return second.decode(ops, input).mapError(s -> String.format(this.error, this, this.first, this.second, firstError, s)).map(vo -> vo.mapFirst(Either::right));
    }

    @Override
    public <T> DataResult<T> encode(final DynamicOps<T> ops, final Either<F, S> input, final T prefix) {
        return input.map(
                value1 -> this.first.encode(ops, value1, prefix),
                value2 -> this.second.encode(ops, value2, prefix)
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
        return Objects.equals(this.first, eitherCodec.first) && Objects.equals(this.second, eitherCodec.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.first, this.second);
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

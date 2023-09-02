package fr.frinn.custommachinery.impl.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import fr.frinn.custommachinery.api.codec.NamedCodec;

import java.util.Objects;

public class PairCodec<F, S> implements NamedCodec<Pair<F, S>> {

    private final NamedCodec<F> first;
    private final NamedCodec<S> second;

    public static <F, S> PairCodec<F, S> of(NamedCodec<F> first, NamedCodec<S> second) {
        return new PairCodec<>(first, second);
    }

    private PairCodec(final NamedCodec<F> first, final NamedCodec<S> second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public <T> DataResult<Pair<Pair<F, S>, T>> decode(final DynamicOps<T> ops, final T input) {
        return first.decode(ops, input).flatMap(p1 ->
                second.decode(ops, p1.getSecond()).map(p2 ->
                        Pair.of(Pair.of(p1.getFirst(), p2.getFirst()), p2.getSecond())
                )
        );
    }

    @Override
    public <T> DataResult<T> encode(final DynamicOps<T> ops, final Pair<F, S> value, final T rest) {
        return this.second.encode(ops, value.getSecond(), rest).flatMap(f -> first.encode(ops, value.getFirst(), f));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;

        if(o instanceof PairCodec<?,?> codec)
            return Objects.equals(this.first, codec.first) && Objects.equals(this.second, codec.second);

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String name() {
        return "Pair<" + this.first.name() + ", " + this.second.name() + ">";
    }
}

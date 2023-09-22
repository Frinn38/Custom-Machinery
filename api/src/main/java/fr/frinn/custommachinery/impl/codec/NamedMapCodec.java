package fr.frinn.custommachinery.impl.codec;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.CompressorHolder;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapCodec.ResultFunction;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import fr.frinn.custommachinery.api.codec.NamedCodec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public abstract class NamedMapCodec<A> extends CompressorHolder implements MapDecoder<A>, MapEncoder<A>, NamedCodec<A> {

    public static <A> NamedMapCodec<A> of(MapDecoder<A> decoder, MapEncoder<A> encoder, String name) {
        return new NamedMapCodec<A>() {
            @Override
            public <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input) {
                return decoder.decode(ops, input);
            }

            @Override
            public <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                return encoder.encode(input, ops, prefix);
            }

            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.concat(encoder.keys(ops), decoder.keys(ops));
            }

            @Override
            public String name() {
                return name;
            }
        };
    }

    public <O> NamedRecordCodec<O, A> forGetter(Function<O, A> getter) {
        return NamedRecordCodec.of(getter, this);
    }

    @Override
    public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
        return compressedDecode(ops, input).map(r -> Pair.of(r, input));
    }

    @Override
    public <T> DataResult<T> encode(final DynamicOps<T> ops, final A input, final T prefix) {
        return encode(input, ops, compressedBuilder(ops)).build(prefix);
    }

    @Override
    public NamedMapCodec<A> withLifecycle(final Lifecycle lifecycle) {
        return new NamedMapCodec<>() {
            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return NamedMapCodec.this.keys(ops);
            }

            @Override
            public <T> DataResult<A> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                return NamedMapCodec.this.decode(ops, input).setLifecycle(lifecycle);
            }

            @Override
            public <T> RecordBuilder<T> encode(final A input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                return NamedMapCodec.this.encode(input, ops, prefix).setLifecycle(lifecycle);
            }

            @Override
            public String name() {
                return NamedMapCodec.this.name();
            }

            @Override
            public String toString() {
                return NamedMapCodec.this.toString();
            }
        };
    }

    public MapCodec<A> mapCodec() {
        return MapCodec.of(this, this);
    }

    public NamedMapCodec<A> mapResult(final ResultFunction<A> function) {
        return new NamedMapCodec<>() {
            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return NamedMapCodec.this.keys(ops);
            }

            @Override
            public <T> RecordBuilder<T> encode(final A input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                return function.coApply(ops, input, NamedMapCodec.this.encode(input, ops, prefix));
            }

            @Override
            public <T> DataResult<A> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                return function.apply(ops, input, NamedMapCodec.this.decode(ops, input));
            }

            @Override
            public String name() {
                return NamedMapCodec.this.name();
            }
        };
    }

    public NamedMapCodec<A> orElse(final Consumer<String> onError, final A value) {
        return orElse(DataFixUtils.consumerToFunction(onError), value);
    }

    public NamedMapCodec<A> orElse(final UnaryOperator<String> onError, final A value) {
        return mapResult(new ResultFunction<A>() {
            @Override
            public <T> DataResult<A> apply(final DynamicOps<T> ops, final MapLike<T> input, final DataResult<A> a) {
                return DataResult.success(a.mapError(onError).result().orElse(value));
            }

            @Override
            public <T> RecordBuilder<T> coApply(final DynamicOps<T> ops, final A input, final RecordBuilder<T> t) {
                return t.mapError(onError);
            }
        });
    }

    public NamedMapCodec<A> orElseGet(final Consumer<String> onError, final Supplier<? extends A> value) {
        return orElseGet(DataFixUtils.consumerToFunction(onError), value);
    }

    public NamedMapCodec<A> orElseGet(final UnaryOperator<String> onError, final Supplier<? extends A> value) {
        return mapResult(new ResultFunction<A>() {
            @Override
            public <T> DataResult<A> apply(final DynamicOps<T> ops, final MapLike<T> input, final DataResult<A> a) {
                return DataResult.success(a.mapError(onError).result().orElseGet(value));
            }

            @Override
            public <T> RecordBuilder<T> coApply(final DynamicOps<T> ops, final A input, final RecordBuilder<T> t) {
                return t.mapError(onError);
            }
        });
    }

    public NamedMapCodec<A> orElse(final A value) {
        return mapResult(new ResultFunction<A>() {
            @Override
            public <T> DataResult<A> apply(final DynamicOps<T> ops, final MapLike<T> input, final DataResult<A> a) {
                return DataResult.success(a.result().orElse(value));
            }

            @Override
            public <T> RecordBuilder<T> coApply(final DynamicOps<T> ops, final A input, final RecordBuilder<T> t) {
                return t;
            }
        });
    }

    public NamedMapCodec<A> orElseGet(final Supplier<? extends A> value) {
        return mapResult(new ResultFunction<>() {
            @Override
            public <T> DataResult<A> apply(final DynamicOps<T> ops, final MapLike<T> input, final DataResult<A> a) {
                return DataResult.success(a.result().orElseGet(value));
            }

            @Override
            public <T> RecordBuilder<T> coApply(final DynamicOps<T> ops, final A input, final RecordBuilder<T> t) {
                return t;
            }
        });
    }

    protected final List<String> aliases = new ArrayList<>();

    public NamedMapCodec<A> aliases(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
        return this;
    }
}

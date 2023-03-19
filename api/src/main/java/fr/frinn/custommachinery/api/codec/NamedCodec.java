package fr.frinn.custommachinery.api.codec;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.architectury.registry.registries.Registrar;
import fr.frinn.custommachinery.impl.codec.DefaultOptionalFieldCodec;
import fr.frinn.custommachinery.impl.codec.EnhancedDispatchCodec;
import fr.frinn.custommachinery.impl.codec.EnhancedEitherCodec;
import fr.frinn.custommachinery.impl.codec.EnhancedListCodec;
import fr.frinn.custommachinery.impl.codec.EnumCodec;
import fr.frinn.custommachinery.impl.codec.EnumMapCodec;
import fr.frinn.custommachinery.impl.codec.FieldCodec;
import fr.frinn.custommachinery.impl.codec.NamedMapCodec;
import fr.frinn.custommachinery.impl.codec.NamedRecordCodec;
import fr.frinn.custommachinery.impl.codec.NamedRecordCodec.Instance;
import fr.frinn.custommachinery.impl.codec.NamedRecordCodec.Mu;
import fr.frinn.custommachinery.impl.codec.NumberCodec;
import fr.frinn.custommachinery.impl.codec.OptionalFieldCodec;
import fr.frinn.custommachinery.impl.codec.RegistrarCodec;
import fr.frinn.custommachinery.impl.codec.UnboundedMapCodec;
import io.netty.handler.codec.EncoderException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public interface NamedCodec<A> {

    /** Static **/

    static <A> NamedCodec<A> of(Codec<A> codec) {
        return of(codec, codec.toString());
    }

    static <A> NamedCodec<A> of(Codec<A> codec, String name) {
        return new NamedCodec<>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
                return codec.decode(ops, input);
            }

            @Override
            public <T> DataResult<T> encode(DynamicOps<T> ops, A input, T prefix) {
                return codec.encode(input, ops, prefix);
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    static <A> NamedCodec<List<A>> list(NamedCodec<A> codec) {
        return EnhancedListCodec.of(codec);
    }

    static <A> NamedCodec<List<A>> list(NamedCodec<A> codec, String name) {
        return EnhancedListCodec.of(codec, name);
    }

    static <F, S> NamedCodec<Either<F, S>> either(NamedCodec<F> first, NamedCodec<S> second) {
        return EnhancedEitherCodec.of(first, second);
    }

    static <F, S> NamedCodec<Either<F, S>> either(NamedCodec<F> first, NamedCodec<S> second, String name) {
        return EnhancedEitherCodec.of(first, second, name);
    }

    static <E extends Enum<E>> NamedCodec<E> enumCodec(Class<E> enumClass) {
        return EnumCodec.of(enumClass);
    }

    static <E extends Enum<E>> NamedCodec<E> enumCodec(Class<E> enumClass, String name) {
        return EnumCodec.of(enumClass, name);
    }

    static <A> NamedCodec<A> registrar(Registrar<A> registrar) {
        return RegistrarCodec.of(registrar, false);
    }

    static <K extends Enum<K>, V> NamedMapCodec<Map<K, V>> enumMap(Class<K> keyEnumClass, NamedCodec<V> valueCodec) {
        return EnumMapCodec.of(keyEnumClass, valueCodec);
    }

    static <K extends Enum<K>, V> NamedMapCodec<Map<K, V>> enumMap(Class<K> keyEnumClass, NamedCodec<V> valueCodec, @Nullable V defaultValue) {
        return EnumMapCodec.of(keyEnumClass, valueCodec, defaultValue);
    }

    static <K extends Enum<K>, V> NamedMapCodec<Map<K, V>> enumMap(Class<K> keyEnumClass, NamedCodec<V> valueCodec, String name) {
        return EnumMapCodec.of(keyEnumClass, valueCodec, name);
    }

    static <K extends Enum<K>, V> NamedMapCodec<Map<K, V>> enumMap(Class<K> keyEnumClass, NamedCodec<V> valueCodec, @Nullable V defaultValue, String name) {
        return EnumMapCodec.of(keyEnumClass, valueCodec, defaultValue, name);
    }

    static <A> NamedCodec<A> unit(A defaultValue) {
        return unit(defaultValue, defaultValue.toString());
    }

    static <A> NamedCodec<A> unit(A defaultValue, String name) {
        return unit(() -> defaultValue, name);
    }

    static <A> NamedCodec<A> unit(Supplier<A> defaultValue, String name) {
        return new NamedCodec<>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
                return DataResult.success(Pair.of(defaultValue.get(), ops.empty()));
            }

            @Override
            public <T> DataResult<T> encode(DynamicOps<T> ops, A input, T prefix) {
                return ops.mapBuilder().build(prefix);
            }

            @Override
            public String name() {
                return name;
            }
        };
    }

    static <O> NamedMapCodec<O> record(Function<Instance<O>, ? extends App<Mu<O>, O>> builder, String name) {
        return NamedRecordCodec.create(builder, name);
    }

    static <N extends Number & Comparable<N>> Function<N, DataResult<N>> checkRange(final N minInclusive, final N maxInclusive) {
        return value -> {
            if (value.compareTo(minInclusive) >= 0 && value.compareTo(maxInclusive) <= 0) {
                return DataResult.success(value);
            }
            return DataResult.error("Value " + value + " outside of range [" + minInclusive + ":" + maxInclusive + "]", value);
        };
    }

    static NamedCodec<Integer> intRange(int minInclusive, int maxInclusive) {
        final Function<Integer, DataResult<Integer>> checker = checkRange(minInclusive, maxInclusive);
        return INT.flatXmap(checker, checker, "Range: [" + minInclusive + ',' + maxInclusive + ']');
    }

    static NamedCodec<Float> floatRange(float minInclusive, float maxInclusive) {
        final Function<Float, DataResult<Float>> checker = checkRange(minInclusive, maxInclusive);
        return FLOAT.flatXmap(checker, checker, "Range: [" + minInclusive + ',' + maxInclusive + ']');
    }

    static NamedCodec<Double> doubleRange(double minInclusive, double maxInclusive) {
        final Function<Double, DataResult<Double>> checker = checkRange(minInclusive, maxInclusive);
        return DOUBLE.flatXmap(checker, checker, "Range: [" + minInclusive + ',' + maxInclusive + ']');
    }

    static NamedCodec<Long> longRange(long minInclusive, long maxInclusive) {
        final Function<Long, DataResult<Long>> checker = checkRange(minInclusive, maxInclusive);
        return LONG.flatXmap(checker, checker, "Range: [" + minInclusive + ',' + maxInclusive + ']');
    }

    static <A> NamedCodec<A> lazy(Supplier<NamedCodec<A>> supplier, String name) {
        return new NamedCodec<A>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
                return supplier.get().decode(ops, input);
            }

            @Override
            public <T> DataResult<T> encode(DynamicOps<T> ops, A input, T prefix) {
                return supplier.get().encode(ops, input, prefix);
            }

            @Override
            public String name() {
                return name;
            }
        };
    }

    static <K, V> UnboundedMapCodec<K, V> unboundedMap(NamedCodec<K> keyCodec, NamedCodec<V> valueCodec, String name) {
        return UnboundedMapCodec.of(keyCodec, valueCodec, name);
    }

    /** Decoder **/

    <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input);

    default <T> DataResult<A> read(final DynamicOps<T> ops, final T input) {
        return decode(ops, input).map(Pair::getFirst);
    }

    /** Encoder **/

    <T> DataResult<T> encode(final DynamicOps<T> ops, final A input, final T prefix);

    default <T> DataResult<T> encodeStart(final DynamicOps<T> ops, final A input) {
        return encode(ops, input, ops.empty());
    }

    /** Named Codec **/

    String name();

    default Codec<A> codec() {
        return new Codec<A>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
                return NamedCodec.this.decode(ops, input);
            }

            @Override
            public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
                return NamedCodec.this.encode(ops, input, prefix);
            }

            @Override
            public String toString() {
                return NamedCodec.this.name();
            }
        };
    }

    default NamedCodec<List<A>> listOf() {
        return list(this);
    }

    default NamedCodec<List<A>> listOf(String name) {
        return list(this, name);
    }

    default <E> NamedMapCodec<E> dispatch(Function<? super E, ? extends A> type, Function<? super A, ? extends NamedCodec<? extends E>> valueCodecGetter, String name) {
        return dispatch("type", type, valueCodecGetter, name);
    }

    default <E> NamedMapCodec<E> dispatch(String typeKey, Function<? super E, ? extends A> type, Function<? super A, ? extends NamedCodec<? extends E>> valueCodecGetter, String name) {
        return EnhancedDispatchCodec.of(typeKey, this, type.andThen(DataResult::success), valueCodecGetter.andThen(DataResult::success), name);
    }

    default <S> NamedCodec<S> xmap(Function<? super A, ? extends S> to, Function<? super S, ? extends A> from, String name) {
        return new NamedCodec<S>() {
            @Override
            public <T> DataResult<Pair<S, T>> decode(DynamicOps<T> ops, T input) {
                return NamedCodec.this.decode(ops, input).map(p -> p.mapFirst(to));
            }

            @Override
            public <T> DataResult<T> encode(DynamicOps<T> ops, S input, T prefix) {
                return NamedCodec.this.encode(ops, from.apply(input), prefix);
            }

            @Override
            public String name() {
                return name;
            }
        };
    }

    default <S> NamedCodec<S> comapFlatMap(Function<? super A, ? extends DataResult<? extends S>> to, Function<? super S, ? extends A> from, String name) {
        return new NamedCodec<S>() {
            @Override
            public <T> DataResult<Pair<S, T>> decode(DynamicOps<T> ops, T input) {
                return NamedCodec.this.decode(ops, input).flatMap(p -> to.apply(p.getFirst()).map(r -> Pair.of(r, p.getSecond())));
            }

            @Override
            public <T> DataResult<T> encode(DynamicOps<T> ops, S input, T prefix) {
                return NamedCodec.this.encode(ops, from.apply(input), prefix);
            }

            @Override
            public String name() {
                return name;
            }
        };
    }

    default <S> NamedCodec<S> flatComapMap(Function<? super A, ? extends S> to, Function<? super S, ? extends DataResult<? extends A>> from, String name) {
        return new NamedCodec<S>() {
            @Override
            public <T> DataResult<Pair<S, T>> decode(DynamicOps<T> ops, T input) {
                return NamedCodec.this.decode(ops, input).map(p -> p.mapFirst(to));
            }

            @Override
            public <T> DataResult<T> encode(DynamicOps<T> ops, S input, T prefix) {
                return from.apply(input).flatMap(a -> NamedCodec.this.encode(ops, a, prefix));
            }

            @Override
            public String name() {
                return name;
            }
        };
    }

    default <S> NamedCodec<S> flatXmap(Function<? super A, ? extends DataResult<? extends S>> to, Function<? super S, ? extends DataResult<? extends A>> from, String name) {
        return new NamedCodec<S>() {
            @Override
            public <T> DataResult<Pair<S, T>> decode(DynamicOps<T> ops, T input) {
                return NamedCodec.this.decode(ops, input).flatMap(p -> to.apply(p.getFirst()).map(r -> Pair.of(r, p.getSecond())));
            }

            @Override
            public <T> DataResult<T> encode(DynamicOps<T> ops, S input, T prefix) {
                return from.apply(input).flatMap(a -> NamedCodec.this.encode(ops, a, prefix));
            }

            @Override
            public String name() {
                return name;
            }
        };
    }

    default NamedMapCodec<A> fieldOf(String fieldName) {
        return FieldCodec.of(fieldName, this, name());
    }

    default NamedMapCodec<Optional<A>> optionalFieldOf(String fieldName) {
        return OptionalFieldCodec.of(fieldName, this, name());
    }

    default NamedMapCodec<A> optionalFieldOf(String fieldName, A defaultValue) {
        return optionalFieldOf(fieldName, () -> defaultValue);
    }

    default NamedMapCodec<A> optionalFieldOf(String fieldName, Supplier<A> defaultValue) {
        return DefaultOptionalFieldCodec.of(fieldName, this, defaultValue, name());
    }

    default void toNetwork(A input, FriendlyByteBuf buf) {
        DataResult<Tag> result = encodeStart(NbtOps.INSTANCE, input);
        result.error().ifPresent(error -> {
            throw new EncoderException(String.format("Failed to encode: %s\nError: %s\nInput: %s", name(), error.message(), input.toString()));
        });
        buf.writeNbt((CompoundTag)result.result().orElseThrow());
    }

    default A fromNetwork(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readAnySizeNbt();
        DataResult<A> result = read(NbtOps.INSTANCE, tag);
        result.error().ifPresent(error -> {
            throw new EncoderException(String.format("Failed to decode: %s\nError: %s\nInput: %S", name(), error.message(), tag));
        });
        return result.result().orElseThrow();
    }

    /** Primitives Codecs **/

    NamedCodec<Boolean> BOOL = new NamedCodec<>() {
        @Override
        public <T> DataResult<Pair<Boolean, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<Boolean> result = ops.getBooleanValue(input);
            if(result.result().isPresent())
                return result.map(b -> Pair.of(b, ops.empty()));
            DataResult<String> stringResult = ops.getStringValue(input);
            if(stringResult.result().isPresent()) {
                String s = stringResult.result().get();
                if(s.equalsIgnoreCase("true"))
                    return DataResult.success(Pair.of(true, ops.empty()));
                else if(s.equalsIgnoreCase("false"))
                    return DataResult.success(Pair.of(false, ops.empty()));
            }
            return result.map(b -> Pair.of(b, input));
        }

        @Override
        public <T> DataResult<T> encode(DynamicOps<T> ops, Boolean input, T prefix) {
            return ops.mergeToPrimitive(prefix, ops.createBoolean(input));
        }

        @Override
        public String name() {
            return "Boolean";
        }
    };
    NamedCodec<Byte> BYTE = new NumberCodec<>() {
        @Override
        public <T> DataResult<Byte> parse(DynamicOps<T> ops, T input) {
            return ops.getNumberValue(input).map(Number::byteValue);
        }

        @Override
        public Byte fromString(String s) throws NumberFormatException {
            return Byte.parseByte(s);
        }

        @Override
        public String name() {
            return "Byte";
        }
    };
    NamedCodec<Short> SHORT = new NumberCodec<>() {
        @Override
        public <T> DataResult<Short> parse(DynamicOps<T> ops, T input) {
            return ops.getNumberValue(input).map(Number::shortValue);
        }

        @Override
        public Short fromString(String s) throws NumberFormatException {
            return Short.parseShort(s);
        }

        @Override
        public String name() {
            return "Short";
        }
    };
    NamedCodec<Integer> INT = new NumberCodec<>() {
        @Override
        public <T> DataResult<Integer> parse(DynamicOps<T> ops, T input) {
            return ops.getNumberValue(input).map(Number::intValue);
        }

        @Override
        public Integer fromString(String s) throws NumberFormatException {
            return Integer.parseInt(s);
        }

        @Override
        public String name() {
            return "Integer";
        }
    };
    NamedCodec<Long> LONG = new NumberCodec<>() {
        @Override
        public <T> DataResult<Long> parse(DynamicOps<T> ops, T input) {
            return ops.getNumberValue(input).map(Number::longValue);
        }

        @Override
        public Long fromString(String s) throws NumberFormatException {
            return Long.parseLong(s);
        }

        @Override
        public String name() {
            return "Long";
        }
    };
    NamedCodec<Float> FLOAT = new NumberCodec<>() {
        @Override
        public <T> DataResult<Float> parse(DynamicOps<T> ops, T input) {
            return ops.getNumberValue(input).map(Number::floatValue);
        }

        @Override
        public Float fromString(String s) throws NumberFormatException {
            return Float.parseFloat(s);
        }

        @Override
        public String name() {
            return "Float";
        }
    };
    NamedCodec<Double> DOUBLE = new NumberCodec<>() {
        @Override
        public <T> DataResult<Double> parse(DynamicOps<T> ops, T input) {
            return ops.getNumberValue(input).map(Number::doubleValue);
        }

        @Override
        public Double fromString(String s) throws NumberFormatException {
            return Double.parseDouble(s);
        }

        @Override
        public String name() {
            return "Double";
        }
    };
    NamedCodec<String> STRING              = NamedCodec.of(Codec.STRING);
    NamedCodec<ByteBuffer> BYTE_BUFFER     = NamedCodec.of(Codec.BYTE_BUFFER);
    NamedCodec<IntStream> INT_STREAM       = NamedCodec.of(Codec.INT_STREAM);
    NamedCodec<LongStream> LONG_STREAM     = NamedCodec.of(Codec.LONG_STREAM);
    NamedCodec<DoubleStream> DOUBLE_STREAM = new NamedCodec<>() {
        @Override
        public <T> DataResult<Pair<DoubleStream, T>> decode(DynamicOps<T> ops, T input) {
            return ops.getStream(input).flatMap(stream -> {
                final List<T> list = stream.toList();
                if (list.stream().allMatch(element -> ops.getNumberValue(element).result().isPresent()))
                    return DataResult.success(list.stream().mapToDouble(element -> ops.getNumberValue(element).result().get().doubleValue()));
                return DataResult.error("Some elements are not doubles: " + input);
            }).map(r -> Pair.of(r, ops.empty()));
        }

        @Override
        public <T> DataResult<T> encode(DynamicOps<T> ops, DoubleStream input, T prefix) {
            return ops.mergeToPrimitive(prefix, ops.createList(input.mapToObj(ops::createDouble)));
        }

        @Override
        public String name() {
            return "DoubleStream";
        }
    };
}

package fr.frinn.custommachinery.api.utils;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.KeyDispatchCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.CustomMachineryAPI;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class CodecLogger {

    public static <T, E> Codec<E> loggedDispatch(final Codec<T> keyCodec, final Function<? super E, ? extends T> type, final Function<? super T, ? extends Codec<? extends E>> codec, String name) {
        return dispatch(keyCodec, type.andThen(DataResult::success), codec.andThen(DataResult::success), name);
    }

    private static <K, V> Codec<V> dispatch(final Codec<K> keyCodec, final Function<? super V, ? extends DataResult<? extends K>> type, final Function<? super K, ? extends DataResult<? extends Codec<? extends V>>> codec, String name) {
        return KeyDispatchCodec.unsafe("type", keyCodec, type, codec, v -> getCodec(type, codec, v)).codec().mapResult(new Codec.ResultFunction<V>() {
            @Override
            public <T> DataResult<Pair<V, T>> apply(DynamicOps<T> ops, T input, DataResult<Pair<V, T>> a) {
                return a.mapError(s -> {
                    String[] words = s.split(" ");
                    if(words.length >= 5 && words[0].equals("No") && words[1].equals("key") && words[3].equals("in"))
                        s = String.format("Error while deserializing: %s%nMissing mandatory property: %s", ops.getGeneric(input, ops.createString("type")).result().orElse(ops.createString("")), words[2]);
                    return String.format("Error while deserializing %s: %s%n%s", name, ops.getGeneric(input, ops.createString("type")).result().orElse(ops.createString("")), s);
                });
            }

            @Override
            public <T> DataResult<T> coApply(DynamicOps<T> ops, V input, DataResult<T> t) {
                return t.mapError(s -> String.format("Error while serializing %s: %s%n%s", name, type.apply(input).result().orElse(null), s));
            }
        }).promotePartial(CustomMachineryAPI::error);
    }
    @SuppressWarnings("unchecked")
    private static <K, V> DataResult<? extends Encoder<V>> getCodec(final Function<? super V, ? extends DataResult<? extends K>> type, final Function<? super K, ? extends DataResult<? extends Encoder<? extends V>>> encoder, final V input) {
        return type.apply(input).<Encoder<? extends V>>flatMap(k -> encoder.apply(k).map(Function.identity())).map(c -> ((Encoder<V>) c));
    }

    public static <T> Codec<T> loggedRecord(final Function<RecordCodecBuilder.Instance<T>, ? extends App<RecordCodecBuilder.Mu<T>, T>> builder) {
        return RecordCodecBuilder.create(builder).mapResult(new Codec.ResultFunction<T>() {
            @Override
            public <T1> DataResult<Pair<T, T1>> apply(DynamicOps<T1> ops, T1 input, DataResult<Pair<T, T1>> a) {
                return a.mapError(s -> {
                    String[] words = s.split(" ");
                    if(words.length >= 5 && words[0].equals("No") && words[1].equals("key") && words[3].equals("in"))
                        return String.format("Error while deserializing: %s%nMissing mandatory property: %s", ops.getGeneric(input, ops.createString("type")).result().orElse(ops.createString("")), words[2]);
                    return s;
                });
            }

            @Override
            public <T1> DataResult<T1> coApply(DynamicOps<T1> ops, T input, DataResult<T1> t) {
                return t;
            }
        });
    }

    public static <T> Codec<T> namedCodec(Codec<T> codec, String name) {
        return new Codec<T>() {
            @Override
            public <T1> DataResult<Pair<T, T1>> decode(DynamicOps<T1> ops, T1 input) {
                return codec.decode(ops, input);
            }

            @Override
            public <T1> DataResult<T1> encode(T input, DynamicOps<T1> ops, T1 prefix) {
                return codec.encode(input, ops, prefix);
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    public static <E> MapCodec<Optional<E>> loggedOptional(Codec<E> codec, String field) {
        return codec.optionalFieldOf(field);
    }

    public static <E> MapCodec<E> loggedOptional(Codec<E> codec, String field, E defaultValue) {
        return new MapCodec<E>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return codec.fieldOf(field).keys(ops);
            }

            @Override
            public <T> DataResult<E> decode(DynamicOps<T> ops, MapLike<T> input) {
                T value = input.get(field);
                if(value == null) {
                    if(CMLogger.shouldLogMissingOptionals())
                        CustomMachineryAPI.warn("Missing optional property: \"%s\" of type: %s%nUsing default value: %s", field, codec, defaultValue);
                    return DataResult.success(defaultValue);
                }
                DataResult<E> result = codec.parse(ops, value);
                if(result.error().isPresent()) {
                    CustomMachineryAPI.warn("Error while deserializing optional property \"%s\" of type: %s%n%s%nUsing default value: %s", field, codec, result.error().get().message(), defaultValue);
                    return DataResult.success(defaultValue);
                }
                return result;
            }

            @Override
            public <T> RecordBuilder<T> encode(E input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                if(Objects.equals(input, defaultValue))
                    return prefix;
                return prefix.add(field, codec.encodeStart(ops, input));
            }
        };
    }
}

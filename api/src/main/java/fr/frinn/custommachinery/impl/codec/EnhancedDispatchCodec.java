package fr.frinn.custommachinery.impl.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import fr.frinn.custommachinery.api.codec.NamedCodec;

import java.util.function.Function;
import java.util.stream.Stream;

public class EnhancedDispatchCodec<K, V> extends NamedMapCodec<V> {

    private final String typeKey;
    private final NamedCodec<K> keyCodec;
    private final Function<? super V, ? extends DataResult<? extends K>> type;
    private final String valueKey = "value";
    private final Function<? super K, ? extends DataResult<? extends NamedCodec<? extends V>>> valueCodec;
    private final String name;

    public static <K, V> EnhancedDispatchCodec<K, V> of(String typeKey, final NamedCodec<K> keyCodec, Function<? super V, ? extends DataResult<? extends K>> type, Function<? super K, ? extends DataResult<? extends NamedCodec<? extends V>>> decoder, String name) {
        return new EnhancedDispatchCodec<>(typeKey, keyCodec, type, decoder, name);
    }

    private EnhancedDispatchCodec(String typeKey, NamedCodec<K> keyCodec, Function<? super V, ? extends DataResult<? extends K>> type, Function<? super K, ? extends DataResult<? extends NamedCodec<? extends V>>> valueCodec, String name) {
        this.typeKey = typeKey;
        this.keyCodec = keyCodec;
        this.type = type;
        this.valueCodec = valueCodec;
        this.name = name;
    }

    @Override
    public <T> DataResult<V> decode(final DynamicOps<T> ops, final MapLike<T> input) {
        T elementName = input.get(typeKey);
        if(elementName == null) {
            for(String alias : this.aliases) {
                elementName = input.get(alias);
                if(elementName != null)
                    break;
            }
        }
        if (elementName == null) {
            return DataResult.error("Input does not contain a key [" + typeKey + "]: " + input);
        }

        return keyCodec.decode(ops, elementName).flatMap(type -> {
            final DataResult<? extends NamedCodec<? extends V>> elementDecoder = valueCodec.apply(type.getFirst());
            return elementDecoder.flatMap(c -> {
                if (ops.compressMaps()) {
                    final T value = input.get(ops.createString(valueKey));
                    if (value == null) {
                        return DataResult.error("Input does not have a \"value\" entry: " + input);
                    }
                    return c.read(ops, value).map(Function.identity());
                }

                return c.decode(ops, ops.createMap(input.entries())).map(Pair::getFirst);
            });
        });
    }

    @Override
    public <T> RecordBuilder<T> encode(final V input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
        final DataResult<? extends NamedCodec<V>> elementEncoder = getCodec(type, valueCodec, input);
        final RecordBuilder<T> builder = prefix.withErrorsFrom(elementEncoder);
        if (elementEncoder.result().isEmpty()) {
            return builder;
        }

        final NamedCodec<V> c = elementEncoder.result().get();
        if (ops.compressMaps()) {
            return prefix
                    .add(typeKey, type.apply(input).flatMap(t -> keyCodec.encodeStart(ops, t)))
                    .add(valueKey, c.encodeStart(ops, input));
        }

        final T typeString = ops.createString(typeKey);

        final DataResult<T> result = c.encodeStart(ops, input);
        final DataResult<MapLike<T>> element = result.flatMap(ops::getMap);
        return element.map(map -> {
            prefix.add(typeString, type.apply(input).flatMap(t -> keyCodec.encodeStart(ops, t)));
            map.entries().forEach(pair -> {
                if (!pair.getFirst().equals(typeString)) {
                    prefix.add(pair.getFirst(), pair.getSecond());
                }
            });
            return prefix;
        }).result().orElseGet(() -> prefix.withErrorsFrom(element));
    }

    @SuppressWarnings("unchecked")
    private static <K, V> DataResult<? extends NamedCodec<V>> getCodec(final Function<? super V, ? extends DataResult<? extends K>> type, final Function<? super K, ? extends DataResult<? extends NamedCodec<? extends V>>> valueCodec, final V input) {
        return type.apply(input).<NamedCodec<? extends V>>flatMap(k -> valueCodec.apply(k).map(Function.identity())).map(c -> (NamedCodec<V>) c);
    }

    @Override
    public <T> Stream<T> keys(final DynamicOps<T> ops) {
        return Stream.of(typeKey, valueKey).map(ops::createString);
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

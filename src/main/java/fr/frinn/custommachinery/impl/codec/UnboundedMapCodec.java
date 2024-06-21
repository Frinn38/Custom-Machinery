package fr.frinn.custommachinery.impl.codec;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.RecordBuilder;
import fr.frinn.custommachinery.api.codec.NamedCodec;

import java.util.Map;

public class UnboundedMapCodec<K, V> implements NamedCodec<Map<K, V>> {

    public static <K, V> UnboundedMapCodec<K, V> of(NamedCodec<K> keyCodec, NamedCodec<V> valueCodec, String name) {
        return new UnboundedMapCodec<>(keyCodec, valueCodec, name);
    }

    private final NamedCodec<K> keyCodec;
    private final NamedCodec<V> valueCodec;
    private final String name;

    private UnboundedMapCodec(NamedCodec<K> keyCodec, NamedCodec<V> valueCodec, String name) {
        this.keyCodec = keyCodec;
        this.valueCodec = valueCodec;
        this.name = name;
    }

    @Override
    public <T> DataResult<Pair<Map<K, V>, T>> decode(DynamicOps<T> ops, T input) {
        return ops.getMap(input).flatMap(map -> {
            final ImmutableMap.Builder<K, V> read = ImmutableMap.builder();
            final ImmutableList.Builder<Pair<T, T>> failed = ImmutableList.builder();

            final DataResult<Unit> result = map.entries().reduce(
                    DataResult.success(Unit.INSTANCE, Lifecycle.stable()),
                    (r, pair) -> {
                        final DataResult<K> k = keyCodec.read(ops, pair.getFirst());
                        final DataResult<V> v = valueCodec.read(ops, pair.getSecond());

                        final DataResult<Pair<K, V>> entry = k.apply2stable(Pair::of, v);
                        entry.error().ifPresent(e -> failed.add(pair));

                        return r.apply2stable((u, p) -> {
                            read.put(p.getFirst(), p.getSecond());
                            return u;
                        }, entry);
                    },
                    (r1, r2) -> r1.apply2stable((u1, u2) -> u1, r2)
            );

            final Map<K, V> elements = read.build();
            final T errors = ops.createMap(failed.build().stream());

            return result.map(unit -> elements).setPartial(elements).mapError(e -> e + " missed input: " + errors);
        }).map(r -> Pair.of(r, input));
    }

    @Override
    public <T> DataResult<T> encode(DynamicOps<T> ops, Map<K, V> input, T prefix) {
        RecordBuilder<T> builder = ops.mapBuilder();
        for (final Map.Entry<K, V> entry : input.entrySet()) {
            builder.add(keyCodec.encodeStart(ops, entry.getKey()), valueCodec.encodeStart(ops, entry.getValue()));
        }
        return builder.build(prefix);
    }

    @Override
    public String name() {
        return this.name;
    }
}

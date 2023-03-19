package fr.frinn.custommachinery.impl.codec;

import com.google.common.collect.Maps;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

public class EnumMapCodec<K extends Enum<K>, V> extends NamedMapCodec<Map<K, V>> {

    public static <K extends Enum<K>, V> EnumMapCodec<K, V> of(Class<K> keyEnum, NamedCodec<V> valueCodec) {
        return of(keyEnum, valueCodec, null, "EnumMap<" + keyEnum.getSimpleName() + ", " + valueCodec.name() + ">");
    }

    public static <K extends Enum<K>, V> EnumMapCodec<K, V> of(Class<K> keyEnum, NamedCodec<V> valueCodec, @Nullable V defaultValue) {
        return of(keyEnum, valueCodec, defaultValue, "EnumMap<" + keyEnum.getSimpleName() + ", " + valueCodec.name() + ">");
    }

    public static <K extends Enum<K>, V> EnumMapCodec<K, V> of(Class<K> keyEnum, NamedCodec<V> valueCodec, String name) {
        return of(keyEnum, valueCodec, null, name);
    }

    public static <K extends Enum<K>, V> EnumMapCodec<K, V> of(Class<K> keyEnum, NamedCodec<V> valueCodec, @Nullable V defaultValue, String name) {
        return new EnumMapCodec<>(keyEnum, valueCodec, defaultValue, name);
    }

    private final Class<K> keyEnum;
    private final NamedCodec<K> keyCodec;
    private final NamedCodec<V> valueCodec;
    @Nullable
    private final V defaultValue;
    private final String name;

    private EnumMapCodec(Class<K> keyEnum, NamedCodec<V> valueCodec, @Nullable V defaultValue, String name) {
        this.keyEnum = keyEnum;
        this.keyCodec = EnumCodec.of(keyEnum);
        this.valueCodec = valueCodec;
        this.defaultValue = defaultValue;
        this.name = name;
    }

    @Override
    public <T> Stream<T> keys(DynamicOps<T> ops) {
        return Arrays.stream(this.keyEnum.getEnumConstants()).map(k -> ops.createString(k.toString()));
    }

    @Override
    public <T> DataResult<Map<K, V>> decode(DynamicOps<T> ops, MapLike<T> input) {
        Map<K, V> map = Maps.newEnumMap(this.keyEnum);

        V defaultValue = this.defaultValue;
        if(input.get("default") != null) {
            DataResult<V> defaultResult = this.valueCodec.read(ops, input.get("default"));
            if(defaultResult.result().isPresent())
                defaultValue = defaultResult.result().get();
            else if(defaultResult.error().isPresent())
                ICustomMachineryAPI.INSTANCE.logger().warn("Couldn't parse value for key {} in MapLike {}, {}", "default", input.toString(), defaultResult.error().get().message());
        }

        input.entries().forEach(entry -> {
            DataResult<K> keyResult = this.keyCodec.read(ops, entry.getFirst());
            if(keyResult.result().isPresent()) {
                K key = keyResult.result().get();
                DataResult<V> valueResult = this.valueCodec.read(ops, entry.getSecond());
                if(valueResult.result().isPresent())
                    map.put(key, valueResult.result().get());
                else if(valueResult.error().isPresent())
                    ICustomMachineryAPI.INSTANCE.logger().warn("Couldn't parse value for key {} in MapLike {}, {}", key.toString(), input.toString(), valueResult.error().get().message());
            }
        });

        if(defaultValue != null)
            for(K key : this.keyEnum.getEnumConstants())
                if(!map.containsKey(key))
                    map.put(key, defaultValue);

        return DataResult.success(map);
    }

    @Override
    public <T> RecordBuilder<T> encode(Map<K, V> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        input.forEach((key, value) -> prefix.add(this.keyCodec.encodeStart(ops, key), this.valueCodec.encodeStart(ops, value)));
        return prefix;
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

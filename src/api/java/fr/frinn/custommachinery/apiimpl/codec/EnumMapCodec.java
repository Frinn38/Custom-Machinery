package fr.frinn.custommachinery.apiimpl.codec;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

public class EnumMapCodec<K extends Enum<K>, V> extends MapCodec<Map<K, V>> {

    public static <K extends Enum<K>, V> EnumMapCodec<K, V> of(Class<K> keyEnum, Codec<K> keyCodec, Codec<V> valueCodec) {
        return new EnumMapCodec<>(keyEnum, keyCodec, valueCodec, null);
    }

    public static <K extends Enum<K>, V> EnumMapCodec<K, V> of(Class<K> keyEnum, Codec<K> keyCodec, Codec<V> valueCodec, V defaultValue) {
        return new EnumMapCodec<>(keyEnum, keyCodec, valueCodec, defaultValue);
    }

    private final Class<K> keyEnum;
    private final Codec<K> keyCodec;
    private final Codec<V> valueCodec;
    @Nullable
    private final V defaultValue;

    public EnumMapCodec(Class<K> keyEnum, Codec<K> keyCodec, Codec<V> valueCodec, @Nullable V defaultValue) {
        this.keyEnum = keyEnum;
        this.keyCodec = keyCodec;
        this.valueCodec = valueCodec;
        this.defaultValue = defaultValue;
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
            DataResult<V> defaultResult = this.valueCodec.parse(ops, input.get("default"));
            if(defaultResult.result().isPresent())
                defaultValue = defaultResult.result().get();
            else if(defaultResult.error().isPresent())
                ICustomMachineryAPI.INSTANCE.logger().warn("Couldn't parse value for key {} in MapLike {}, {}", "default", input.toString(), defaultResult.error().get().message());
        }

        input.entries().forEach(entry -> {
            DataResult<K> keyResult = this.keyCodec.parse(ops, entry.getFirst());
            if(keyResult.result().isPresent()) {
                K key = keyResult.result().get();
                DataResult<V> valueResult = this.valueCodec.parse(ops, entry.getSecond());
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
    public String toString() {
        return "EnumMap[" + this.keyCodec.toString() + ", " + this.valueCodec.toString() + "]";
    }
}

package fr.frinn.custommachinery.impl.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class FieldCodec<A> extends NamedMapCodec<A> {

    public static <A> NamedMapCodec<A> of(String fieldName, NamedCodec<A> elementCodec, String name) {
        return new FieldCodec<>(fieldName, elementCodec, name);
    }

    private final String fieldName;
    private final NamedCodec<A> elementCodec;
    private final String name;

    protected FieldCodec(String fieldName, NamedCodec<A> elementCodec, String name) {
        this.fieldName = toSnakeCase(fieldName);
        this.elementCodec = elementCodec;
        this.name = name;
    }

    public FieldCodec<A> aliases(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
        return this;
    }

    @Override
    public <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input) {
        T value = tryGetValue(ops, input, fieldName);
        if(value == null) {
            for(String alias : this.aliases) {
                value = input.get(alias);
                if(value != null)
                    break;
            }
        }
        if(value == null) {
            ICustomMachineryAPI.INSTANCE.logger().error("Missing mandatory property \"{}\" of type \"{}\" in {}", fieldName, name, input);
            return DataResult.error("No key " + fieldName + " in " + input);
        }
        return elementCodec.read(ops, value);
    }

    @Override
    public <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        return prefix.add(fieldName, elementCodec.encodeStart(ops, input));
    }

    @Override
    public <T> Stream<T> keys(DynamicOps<T> ops) {
        return Stream.of(ops.createString(this.fieldName));
    }

    @Override
    public String name() {
        return this.name;
    }

    public static String toSnakeCase(String input) {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        for(char c : input.toCharArray()) {
            if(c == ' ')
                builder.append('_');
            else if(Character.isUpperCase(c)) {
                if(index != 0)
                    builder.append('_');
                builder.append(String.valueOf(c).toLowerCase(Locale.ROOT));
            }
            else
                builder.append(c);
            index++;
        }
        return builder.toString();
    }

    @Nullable
    public static <T> T tryGetValue(DynamicOps<T> ops, MapLike<T> map, String fieldName) {
        return map.entries().filter(pair -> {
            String key = ops.getStringValue(pair.getFirst()).result().orElseThrow();
            return key.equals(fieldName) || toSnakeCase(key).equals(fieldName) || toSnakeCase(key).replace("_", "").equals(fieldName.replace("_", ""));
        }).findFirst().map(Pair::getSecond).orElse(null);
    }
}

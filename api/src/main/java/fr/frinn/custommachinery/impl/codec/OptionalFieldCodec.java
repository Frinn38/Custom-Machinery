package fr.frinn.custommachinery.impl.codec;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.api.codec.NamedCodec;

import java.util.Optional;
import java.util.stream.Stream;

public class OptionalFieldCodec<A> extends NamedMapCodec<Optional<A>> {

    public static <A> OptionalFieldCodec<A> of(String fieldName, NamedCodec<A> elementCodec, String name) {
        return new OptionalFieldCodec<>(fieldName, elementCodec, name);
    }

    private final String fieldName;
    private final NamedCodec<A> elementCodec;
    private final String name;

    private OptionalFieldCodec(String fieldName, NamedCodec<A> elementCodec, String name) {
        this.fieldName = FieldCodec.toSnakeCase(fieldName);
        this.elementCodec = elementCodec;
        this.name = name;
    }

    @Override
    public <T> DataResult<Optional<A>> decode(DynamicOps<T> ops, MapLike<T> input) {
        T value = FieldCodec.tryGetValue(ops, input, fieldName);
        if (value == null) {
            if(ICustomMachineryAPI.INSTANCE.config().logMissingOptional())
                ICustomMachineryAPI.INSTANCE.logger().debug("Missing optional property: \"{}\" of type: {}, using default value", fieldName, name);
            return DataResult.success(Optional.empty());
        }
        DataResult<A> result = elementCodec.read(ops, value);
        if (result.result().isPresent())
            return result.map(Optional::of);
        if(result.error().isPresent())
            ICustomMachineryAPI.INSTANCE.logger().warn("Couldn't parse \"{}\" for key \"{}\", using default value\nError: {}", name, fieldName, result.error().get().message());
        return DataResult.success(Optional.empty());
    }

    @Override
    public <T> RecordBuilder<T> encode(Optional<A> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        if (input.isPresent())
            return prefix.add(fieldName, elementCodec.encodeStart(ops, input.get()));
        return prefix;
    }

    @Override
    public <T> Stream<T> keys(DynamicOps<T> ops) {
        return Stream.of(ops.createString(this.fieldName));
    }

    @Override
    public String name() {
        return this.name;
    }
}

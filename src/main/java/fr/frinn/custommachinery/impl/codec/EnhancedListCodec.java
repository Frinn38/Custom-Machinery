package fr.frinn.custommachinery.impl.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.api.codec.NamedCodec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class EnhancedListCodec<A> implements NamedCodec<List<A>> {

    private final NamedCodec<A> elementCodec;
    private final String name;

    public static <A> EnhancedListCodec<A> of(NamedCodec<A> elementCodec) {
        return of(elementCodec, "List<" + elementCodec.name() + ">");
    }

    public static <A> EnhancedListCodec<A> of(NamedCodec<A> elementCodec, String name) {
        return new EnhancedListCodec<>(elementCodec, name);
    }

    private EnhancedListCodec(final NamedCodec<A> elementCodec, String name) {
        this.elementCodec = elementCodec;
        this.name = name;
    }

    @Override
    public <T> DataResult<T> encode(final DynamicOps<T> ops, final List<A> input, final T prefix) {
        final ListBuilder<T> builder = ops.listBuilder();

        for (final A a : input) {
            builder.add(this.elementCodec.encodeStart(ops, a));
        }

        return builder.build(prefix);
    }

    @Override
    public <T> DataResult<Pair<List<A>, T>> decode(final DynamicOps<T> ops, final T input) {
        //If the input is not a list try to parse it as a single element
        if(ops.getStream(input).error().isPresent())
            return this.elementCodec.decode(ops, input).map(pair -> Pair.of(Collections.singletonList(pair.getFirst()), pair.getSecond()));

        DataResult<Stream<T>> streamResult = ops.getStream(input);
        if(streamResult.result().isPresent()) {
            Stream<T> stream = streamResult.result().get();
            List<A> result = new ArrayList<>();
            stream.forEach(t -> {
                DataResult<A> a = this.elementCodec.read(ops, t);
                if(a.result().isPresent())
                    result.add(a.result().get());
                else if(a.error().isPresent())
                    ICustomMachineryAPI.INSTANCE.logger().warn("Error when parsing {} in list.\n{}", this.elementCodec.name(), a.error().get().message());
            });
            return DataResult.success(Pair.of(result, ops.empty()));
        }
        return streamResult.map(s -> Pair.of(Collections.emptyList(), input));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final EnhancedListCodec<?> listCodec = (EnhancedListCodec<?>) o;
        return Objects.equals(this.elementCodec, listCodec.elementCodec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.elementCodec);
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

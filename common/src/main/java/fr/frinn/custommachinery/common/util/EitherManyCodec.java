package fr.frinn.custommachinery.common.util;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import fr.frinn.custommachinery.api.codec.NamedCodec;

public class EitherManyCodec<T> implements NamedCodec<T> {

    private final String name;
    private final NamedCodec<T> mainCodec;
    private final NamedCodec<T>[] otherCodecs;

    @SafeVarargs
    public static <T> EitherManyCodec<T> of(NamedCodec<T> mainCodec, NamedCodec<T>... otherCodecs) {
        return of(mainCodec.name(), mainCodec, otherCodecs);
    }

    @SafeVarargs
    public static <T> EitherManyCodec<T> of(String name, NamedCodec<T> mainCodec, NamedCodec<T>... otherCodecs) {
        return new EitherManyCodec<>(name, mainCodec, otherCodecs);
    }

    private EitherManyCodec(String name, NamedCodec<T> mainCodec, NamedCodec<T>[] otherCodecs) {
        this.name = name;
        this.mainCodec = mainCodec;
        this.otherCodecs = otherCodecs;
    }

    @Override
    public <O> DataResult<Pair<T, O>> decode(DynamicOps<O> ops, O input) {
        StringBuilder error = new StringBuilder();
        for (NamedCodec<T> codec : Lists.asList(this.mainCodec, otherCodecs)) {
            DataResult<Pair<T, O>> result = codec.decode(ops, input);
            if(result.result().isPresent())
                return result;
            else if(result.error().isPresent())
                error.append(result.error().get().message());
        }
        return DataResult.error(error.toString());
    }

    @Override
    public <O> DataResult<O> encode(DynamicOps<O> ops, T input, O prefix) {
        return this.mainCodec.encode(ops, input, prefix);
    }

    @Override
    public String name() {
        return this.name;
    }
}

package fr.frinn.custommachinery.common.util;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

public class EitherManyCodec<T> implements Codec<T> {

    private final Codec<T> mainCodec;
    private final Codec<T>[] otherCodecs;

    @SafeVarargs
    public static <T> EitherManyCodec<T> of(Codec<T> mainCodec, Codec<T>... otherCodecs) {
        return new EitherManyCodec<>(mainCodec, otherCodecs);
    }

    private EitherManyCodec(Codec<T> mainCodec, Codec<T>[] otherCodecs) {
        this.mainCodec = mainCodec;
        this.otherCodecs = otherCodecs;
    }

    @Override
    public <O> DataResult<Pair<T, O>> decode(DynamicOps<O> ops, O input) {
        StringBuilder error = new StringBuilder();
        for (Codec<T> codec : Lists.asList(this.mainCodec, otherCodecs)) {
            DataResult<Pair<T, O>> result = codec.decode(ops, input);
            if(result.result().isPresent())
                return result;
            else if(result.error().isPresent())
                error.append(result.error().get().message());
        }
        return DataResult.error(error.toString());
    }

    @Override
    public <O> DataResult<O> encode(T input, DynamicOps<O> ops, O prefix) {
        return this.mainCodec.encode(input, ops, prefix);
    }
}

package fr.frinn.custommachinery.api.utils;

import com.mojang.datafixers.util.Either;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.impl.codec.NamedMapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public record Filter<T>(List<Either<TagKey<T>, Holder<T>>> whitelist, List<Either<TagKey<T>, Holder<T>>> blacklist) implements Predicate<T> {

    public static <T> NamedMapCodec<Filter<T>> codec(NamedCodec<Either<TagKey<T>, Holder<T>>> valueCodec) {
        return NamedCodec.record(instance -> instance.group(
                valueCodec.listOf().optionalFieldOf("whitelist", Collections.emptyList()).forGetter(filter -> filter.whitelist),
                valueCodec.listOf().optionalFieldOf("blacklist", Collections.emptyList()).forGetter(filter -> filter.blacklist)
        ).apply(instance, Filter::new), "Filter[" + valueCodec.name() + "]");
    }

    public static <T> Filter<T> empty() {
        return new Filter<>(Collections.emptyList(), Collections.emptyList());
    }

    @Override
    public boolean test(T t) {
        if(this.blacklist.stream().flatMap(either -> either.map(this::valuesFromTag, holder -> Stream.of(holder.value()))).anyMatch(value -> value.equals(t)))
            return false;
        return this.whitelist.isEmpty() || this.whitelist.stream().flatMap(either -> either.map(this::valuesFromTag, holder -> Stream.of(holder.value()))).anyMatch(value -> value.equals(t));
    }

    @SuppressWarnings("unchecked")
    private Stream<T> valuesFromTag(TagKey<T> key) {
        Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(key.registry().location());
        if(registry == null)
            return Stream.empty();
        return registry.getTag(key).map(set -> set.stream().map(Holder::value)).orElse(Stream.empty());
    }
}

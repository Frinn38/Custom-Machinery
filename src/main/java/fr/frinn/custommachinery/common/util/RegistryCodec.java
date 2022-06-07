package fr.frinn.custommachinery.common.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RegistryCodec<V extends IForgeRegistryEntry<V>> implements Codec<V> {

    public static <V extends IForgeRegistryEntry<V>> RegistryCodec<V> of(IForgeRegistry<V> registry, boolean isCM) {
        return new RegistryCodec<>(registry, isCM);
    }

    private final IForgeRegistry<V> registry;
    private final boolean isCM;

    private RegistryCodec(IForgeRegistry<V> registry, boolean isCM) {
        this.registry = registry;
        this.isCM = isCM;
    }

    @Override
    public <T> DataResult<Pair<V, T>> decode(DynamicOps<T> ops, T input) {
        return (this.isCM ? Codecs.CM_LOCATION_CODEC : Codecs.RESOURCE_LOCATION_CODEC).decode(ops, input).flatMap(keyValuePair ->
                !this.registry.containsKey(keyValuePair.getFirst())
                ? DataResult.error("Unknown registry key in " + this.registry.getRegistryKey() + ": " + keyValuePair.getFirst())
                : DataResult.success(keyValuePair.mapFirst(this.registry::getValue))
        );
    }

    @Override
    public <T> DataResult<T> encode(V input, DynamicOps<T> ops, T prefix) {
        return ResourceLocation.CODEC.encode(input.getRegistryName(), ops, prefix);
    }
}

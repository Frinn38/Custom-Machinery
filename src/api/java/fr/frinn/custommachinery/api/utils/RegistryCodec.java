package fr.frinn.custommachinery.api.utils;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Utility class to create a Codec to encode and decode registry objects.
 * @param <T> The IForgeRegistryEntry encoded or decoded by this codec.
 */
public class RegistryCodec<T extends IForgeRegistryEntry<T>> implements Codec<T> {

    private IForgeRegistry<T> registry;

    public RegistryCodec(IForgeRegistry<T> registry) {
        this.registry = registry;
    }

    @Override
    public <D> DataResult<D> encode(T input, DynamicOps<D> ops, D prefix) {
        if(ops.compressMaps())
            throw new IllegalArgumentException("This codec does not support integer ids for registry entries");
        ResourceLocation key = registry.getKey(input);
        if(key == null)
        {
            return DataResult.error("Unknown registry element " + input);
        }
        D toMerge = ops.createString(key.toString());
        return ops.mergeToPrimitive(prefix, toMerge);
    }

    @Override
    public <D> DataResult<Pair<T, D>> decode(DynamicOps<D> ops, D input) {
        if(ops.compressMaps())
            throw new IllegalArgumentException("This codec does not support integer ids for registry entries");
        return ResourceLocation.CODEC.decode(ops, input).flatMap(keyValuePair -> !registry.containsKey(keyValuePair.getFirst()) ?
                DataResult.error("Unknown registry key: " + keyValuePair.getFirst()) :
                DataResult.success(keyValuePair.mapFirst(registry::getValue)));
    }
}

package fr.frinn.custommachinery.api.crafting;

import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Used for registering custom {@link ProcessorType}.
 * All instances of this class must be created and registered using {@link net.neoforged.neoforge.registries.DeferredRegister}.
 * @param <T> The {@link IProcessor} handled by this {@link ProcessorType}.
 */
public class ProcessorType<T extends IProcessor> {

    /**
     * The {@link ResourceKey} pointing to the {@link ProcessorType} vanilla registry.
     * Can be used to create a {@link net.neoforged.neoforge.registries.DeferredRegister} for registering your {@link ProcessorType}.
     */
    public static final ResourceKey<Registry<ProcessorType<? extends IProcessor>>> REGISTRY_KEY = ResourceKey.createRegistryKey(ICustomMachineryAPI.INSTANCE.rl("processor_type"));

    /**
     * A factory method used to create new {@link ProcessorType}.
     * @param codec A {@link NamedCodec} used to parse the specified {@link IProcessorTemplate} from the machine JSON and send it to the client.
     * @param <T> The {@link IProcessor} handled by this {@link ProcessorType}.
     */
    public static <T extends IProcessor> ProcessorType<T> create(NamedCodec<? extends IProcessorTemplate<T>> codec) {
        return new ProcessorType<>(codec);
    }

    private final NamedCodec<? extends IProcessorTemplate<T>> codec;

    /**
     * A constructor for {@link ProcessorType}.
     * Use {@link ProcessorType#create(NamedCodec)} instead.
     */
    private ProcessorType(NamedCodec<? extends IProcessorTemplate<T>> codec) {
        this.codec = codec;
    }

    /**
     * @return A {@link NamedCodec} used to parse the {@link IProcessorTemplate} from the machine JSON and send it to the clients.
     */
    public NamedCodec<? extends IProcessorTemplate<T>> getCodec() {
        return this.codec;
    }

    /**
     * A helper method to get the ID of this {@link ProcessorType}.
     * @return The ID of this {@link ProcessorType}, or null if it is not registered.
     */
    public ResourceLocation getId() {
        ResourceLocation id = ICustomMachineryAPI.INSTANCE.processorRegistrar().getKey(this);
        if(id == null)
            throw new IllegalStateException("Trying to get id for an unregistered processor type");
        return id;
    }
}

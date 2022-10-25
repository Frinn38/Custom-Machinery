package fr.frinn.custommachinery.api.crafting;

import com.mojang.serialization.Codec;
import dev.architectury.core.RegistryEntry;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ProcessorType<T extends IProcessor> extends RegistryEntry<ProcessorType<T>> {

    public static final ResourceKey<Registry<ProcessorType<? extends IProcessor>>> REGISTRY_KEY = ResourceKey.createRegistryKey(ICustomMachineryAPI.INSTANCE.rl("processor_type"));

    private final Codec<? extends IProcessorTemplate<T>> codec;

    public ProcessorType(Codec<? extends IProcessorTemplate<T>> codec) {
        this.codec = codec;
    }

    public Codec<? extends IProcessorTemplate<T>> getCodec() {
        return this.codec;
    }

    public ResourceLocation getId() {
        return ICustomMachineryAPI.INSTANCE.processorRegistrar().getId(this);
    }
}

package fr.frinn.custommachinery.api.machine;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class MachineAppearanceProperty<T> extends ForgeRegistryEntry<MachineAppearanceProperty<?>> {

    public static final ResourceKey<Registry<MachineAppearanceProperty<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(ICustomMachineryAPI.INSTANCE.rl("appearance_property"));

    private final Codec<T> codec;
    private final T defaultValue;

    /**
     * Create a new MachineAppearanceProperty, this MUST be registered to the forge registry.
     * @param codec A codec used to parse the MachineAppearanceProperty from the machine json file and sed it to the client.
     * @param defaultValue The default value for this property, used if the machine creator didn't specify a value in the machine json.
     */
    public MachineAppearanceProperty(Codec<T> codec, T defaultValue) {
        this.codec = codec;
        this.defaultValue = defaultValue;
    }

    public Codec<T> getCodec() {
        return this.codec;
    }

    public T getDefaultValue() {
        return this.defaultValue;
    }
}

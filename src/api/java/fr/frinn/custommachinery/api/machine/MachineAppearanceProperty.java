package fr.frinn.custommachinery.api.machine;

import com.mojang.serialization.Codec;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class MachineAppearanceProperty<T> extends ForgeRegistryEntry<MachineAppearanceProperty<?>> {

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

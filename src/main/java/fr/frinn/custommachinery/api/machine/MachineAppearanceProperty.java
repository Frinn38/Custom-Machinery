package fr.frinn.custommachinery.api.machine;

import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Used for registering custom {@link MachineAppearanceProperty}.
 * Each registered {@link MachineAppearanceProperty} will add a new optional property in the {@link IMachineAppearance}.
 * All instances of this class must be created and registered using {@link Registry} for Fabric or {@link net.neoforged.neoforge.registries.DeferredRegister} for Forge or Architectury.
 * @param <T> The {@link Object} handled by this {@link MachineAppearanceProperty}.
 */
public class MachineAppearanceProperty<T> {

    /**
     * The {@link ResourceKey} pointing to the {@link MachineAppearanceProperty} vanilla registry.
     * Can be used to create a {@link net.neoforged.neoforge.registries.DeferredRegister} for registering your {@link MachineAppearanceProperty}.
     */
    public static final ResourceKey<Registry<MachineAppearanceProperty<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(ICustomMachineryAPI.INSTANCE.rl("appearance_property"));

    /**
     * A factory method to create new {@link MachineAppearanceProperty}.
     * @param codec A codec used to parse the {@link MachineAppearanceProperty} from the machine json file and send it to the client.
     * @param defaultValue The default value for this property, used if the machine creator didn't specify a value in the machine json.
     */
    public static <T> MachineAppearanceProperty<T> create(NamedCodec<T> codec, T defaultValue) {
        return new MachineAppearanceProperty<>(codec, defaultValue);
    }

    private final NamedCodec<T> codec;
    private final T defaultValue;

    /**
     * A constructor for {@link MachineAppearanceProperty}.
     * Use {@link MachineAppearanceProperty#create(NamedCodec, Object)} instead.
     */
    private MachineAppearanceProperty(NamedCodec<T> codec, T defaultValue) {
        this.codec = codec;
        this.defaultValue = defaultValue;
    }

    /**
     * @return A {@link NamedCodec} used to parse/serialize the {@link MachineAppearanceProperty} value.
     */
    public NamedCodec<T> getCodec() {
        return this.codec;
    }

    /**
     * @return The default value for this {@link MachineAppearanceProperty}.
     */
    public T getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * A helper method to get the ID of this {@link MachineAppearanceProperty}.
     * @return The ID of this {@link MachineAppearanceProperty}, or null if it is not registered.
     */
    public ResourceLocation getId() {
        return ICustomMachineryAPI.INSTANCE.appearancePropertyRegistrar().getKey(this);
    }
}

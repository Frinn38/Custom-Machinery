package fr.frinn.custommachinery.api.requirement;

import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.crafting.IProcessor;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Used for registering custom {@link RequirementType}.
 * An {@link IRequirement} MUST be linked to a single {@link RequirementType}.
 * All instances of this class must be created and registered using {@link Registry} for Fabric or {@link net.neoforged.neoforge.registries.DeferredRegister} for Forge or Architectury.
 * @param <T> The {@link IRequirement} handled by this {@link RequirementType}.
 */
public class RequirementType<T extends IRequirement<?>> {

    /**
     * The {@link ResourceKey} pointing to the {@link RequirementType} vanilla registry.
     * Can be used to create a {@link net.neoforged.neoforge.registries.DeferredRegister} for registering your {@link RequirementType}.
     */
    public static final ResourceKey<Registry<RequirementType<? extends IRequirement<?>>>> REGISTRY_KEY = ResourceKey.createRegistryKey(ICustomMachineryAPI.INSTANCE.rl("requirement_type"));

    /**
     * Use this factory method to create new {@link RequirementType} for an {@link IRequirement} that depends on something in-world instead of the machine inventory.
     * The handled {@link IRequirement} will be checked every tick by the machine {@link IProcessor} to see if it's still valid.
     * @param codec A {@link NamedCodec} used to deserialize any {@link IRequirement} of this type from json, and to send it to the client over the network in multiplayer.
     * @return A new {@link RequirementType} that will handle the specified {@link IRequirement}.
     */
    public static <T extends IRequirement<?>> RequirementType<T> world(NamedCodec<T> codec) {
        return new RequirementType<>(codec, true);
    }

    /**
     * Use this factory method to create new {@link RequirementType} for an {@link IRequirement} that depends on the machine inventory.
     * The handled {@link IRequirement} will be checked by the machine {@link IProcessor} ONLY if the machine inventory changed to see if it's still valid.
     * @param codec A {@link NamedCodec} used to deserialize any {@link IRequirement} of this type from json, and to send it to the client over the network in multiplayer.
     * @return A new {@link RequirementType} that will handle the specified {@link IRequirement}.
     */
    public static <T extends IRequirement<?>> RequirementType<T> inventory(NamedCodec<T> codec) {
        return new RequirementType<>(codec, false);
    }

    private final NamedCodec<T> codec;
    private final boolean isWorldRequirement;

    /**
     * A constructor for {@link RequirementType}.
     * Use {@link RequirementType#world(NamedCodec)} instead.
     */
    private RequirementType(NamedCodec<T> codec, boolean isWorldRequirement) {
        this.codec = codec;
        this.isWorldRequirement = isWorldRequirement;
    }

    /**
     * Used by the dispatch codec that deserialize all requirements from the recipe json.
     * @return A codec that can deserialize a requirement of this type from json.
     */
    public NamedCodec<T> getCodec() {
        return this.codec;
    }

    /**
     * Used by the machine crafting manager to determinate if a recipe should be checked.
     * Requirements that return true from this method will always be checked.
     * Otherwise, the requirement will be checked only if the machine inventory changed since last check.
     * @return Whether this requirement check something in-world or in the machine inventory.
     */
    public boolean isWorldRequirement() {
        return isWorldRequirement;
    }

    public ResourceLocation getId() {
        return ICustomMachineryAPI.INSTANCE.requirementRegistrar().getKey(this);
    }

    /**
     * Used to display the name of this requirement to the player, either in a gui or in the log.
     * @return A text component representing the name of this requirement.
     */
    public Component getName() {
        if(getId() == null)
            return Component.literal("unknown");
        return Component.translatable("requirement." + getId().getNamespace() + "." + getId().getPath());
    }
}

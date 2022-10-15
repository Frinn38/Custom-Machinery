package fr.frinn.custommachinery.api.requirement;

import com.mojang.serialization.Codec;
import dev.architectury.core.RegistryEntry;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class RequirementType<T extends IRequirement<?>> extends RegistryEntry<RequirementType<T>> {

    public static final ResourceKey<Registry<RequirementType<? extends IRequirement<?>>>> REGISTRY_KEY = ResourceKey.createRegistryKey(ICustomMachineryAPI.INSTANCE.rl("requirement_type"));

    private final Codec<T> codec;
    private boolean isWorldRequirement = false;

    /**
     * Create a new RequirementType, there must be only 1 instance of this class for each types, and this instance must be registered to the ForgeRegistry.
     * @param codec A codec used to deserialize any requirement of this type from json, and to send it to the client over the network in multiplayer.
     */
    public RequirementType(@Nonnull Codec<T> codec) {
        this.codec = codec;
    }

    /**
     * Set this requirement type as a world requirement, which mean that this requirement does not depend on the machine inventory, but rather on something checked in-world.
     * @return Itself.
     */
    public RequirementType<T> setWorldRequirement() {
        this.isWorldRequirement = true;
        return this;
    }

    /**
     * Used by the dispatch codec that deserialize all requirements from the recipe json.
     * @return A codec that can deserialize a requirement of this type from json.
     */
    @Nonnull
    public Codec<T> getCodec() {
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
        return ICustomMachineryAPI.INSTANCE.requirementRegistrar().getId(this);
    }

    /**
     * Used to display the name of this requirement to the player, either in a gui or in the log.
     * @return A text component representing the name of this requirement.
     */
    public Component getName() {
        if(getId() == null)
            return new TextComponent("unknown");
        return new TranslatableComponent("requirement." + getId().getNamespace() + "." + getId().getPath());
    }
}

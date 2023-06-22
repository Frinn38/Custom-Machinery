package fr.frinn.custommachinery.api.guielement;

import dev.architectury.registry.registries.DeferredRegister;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Used for registering custom gui elements.
 * An {@link IGuiElement} MUST be linked to a single {@link GuiElementType}.
 * All instances of this class must be created and registered using {@link Registry} for Fabric or {@link DeferredRegister} for Forge or Architectury.
 * @param <T> The {@link IGuiElement} handled by this {@link GuiElementType}.
 */
public class GuiElementType<T extends IGuiElement> {

    /**
     * The {@link ResourceKey} pointing to the {@link GuiElementType} vanilla registry.
     * Can be used to create a {@link DeferredRegister} for registering your {@link GuiElementType}.
     */
    public static final ResourceKey<Registry<GuiElementType<? extends IGuiElement>>> REGISTRY_KEY = ResourceKey.createRegistryKey(ICustomMachineryAPI.INSTANCE.rl("gui_element_type"));

    /**
     * A factory method to create new {@link GuiElementType}.
     * @param codec A codec used to parse or serialize all {@link IGuiElement} with this type.
     * @return A {@link GuiElementType} for the specified {@link IGuiElement}.
     * @param <T> The {@link IGuiElement} handled by this {@link GuiElementType}.
     */
    public static <T extends IGuiElement> GuiElementType<T> create(NamedCodec<T> codec) {
        return new GuiElementType<>(codec);
    }

    /**
     * Used to parse the machine json file gui property entry to create the corresponding gui element.
     */
    private final NamedCodec<T> codec;

    /**
     * Constructor for {@link GuiElementType}.
     * Use {@link GuiElementType#create(NamedCodec)} instead.
     */
    private GuiElementType(NamedCodec<T> codec) {
        this.codec = codec;
    }

    /**
     * @return The codec used to parse or serialize all {@link IGuiElement} with this type.
     */
    public NamedCodec<T> getCodec() {
        return this.codec;
    }

    /**
     * A helper method to get the ID of this GuiElementType.
     * @return The ID of this GuiElementType, or null if it is not registered.
     */
    public ResourceLocation getId() {
        return ICustomMachineryAPI.INSTANCE.guiElementRegistrar().getId(this);
    }
}

package fr.frinn.custommachinery.api.guielement;

import com.mojang.serialization.Codec;
import dev.architectury.core.RegistryEntry;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * A ForgeRegistryEntry used for registering custom gui elements.
 * An IGuiElement can have only one type.
 * All instances of this class must be created and registered using RegistryEvent or DeferredRegister.
 * @param <T> The component handled by this type.
 */
public class GuiElementType<T extends IGuiElement> extends RegistryEntry<GuiElementType<T>> {

    public static final ResourceKey<Registry<GuiElementType<? extends IGuiElement>>> REGISTRY_KEY = ResourceKey.createRegistryKey(ICustomMachineryAPI.INSTANCE.rl("gui_element_type"));

    //Used to parse the machine json file gui property entry to create the corresponding gui element.
    private final Codec<T> codec;

    public GuiElementType(Codec<T> codec) {
        this.codec = codec;
    }

    public Codec<T> getCodec() {
        return this.codec;
    }

    public ResourceLocation getId() {
        return ICustomMachineryAPI.INSTANCE.guiElementRegistrar().getId(this);
    }
}

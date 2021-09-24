package fr.frinn.custommachinery.api.guielement;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.api.guielement.jei.JEIIngredientRenderer;
import fr.frinn.custommachinery.impl.guielement.GuiElementRendererRegistry;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A ForgeRegistryEntry used for registering custom gui elements.
 * An IGuiElement can have only one type.
 * All instances of this class must be created and registered using RegistryEvent or DeferredRegister.
 * @param <T> The component handled by this type.
 */
public class GuiElementType<T extends IGuiElement> extends ForgeRegistryEntry<GuiElementType<? extends IGuiElement>> {

    //Used to parse the machine json file gui property entry to create the corresponding gui element.
    private Codec<T> codec;
    private Supplier<Function<T, JEIIngredientRenderer<?, ?>>> jeiRenderer;

    public GuiElementType(Codec<T> codec) {
        this.codec = codec;
    }

    public Codec<T> getCodec() {
        return this.codec;
    }

    public GuiElementType<T> setJeiIngredientType(Supplier<Function<T, JEIIngredientRenderer<?, ?>>> renderer) {
        this.jeiRenderer = renderer;
        return this;
    }

    /**
     * Utility method to return the gui element renderer for this type.
     */
    public IGuiElementRenderer<T> getRenderer() {
        return GuiElementRendererRegistry.getRenderer(this);
    }

    public boolean hasJEIRenderer() {
        return this.jeiRenderer != null;
    }

    public JEIIngredientRenderer<?, ?> getJeiRenderer(T element) {
        return this.jeiRenderer == null ? null : this.jeiRenderer.get().apply(element);
    }
}

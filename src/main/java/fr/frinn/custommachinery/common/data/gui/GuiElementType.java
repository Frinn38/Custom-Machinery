package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.client.render.element.IGuiElementRenderer;
import fr.frinn.custommachinery.client.render.element.jei.JEIIngredientRenderer;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Function;
import java.util.function.Supplier;

public class GuiElementType<T extends IGuiElement> extends ForgeRegistryEntry<GuiElementType<? extends IGuiElement>> {

    private Codec<T> codec;
    private Supplier<IGuiElementRenderer<T>> renderer;
    private Supplier<Function<T, JEIIngredientRenderer<?, ?>>> jeiRenderer;

    public GuiElementType(Codec<T> codec, Supplier<IGuiElementRenderer<T>> renderer) {
        this.codec = codec;
        this.renderer = renderer;
    }

    public Codec<T> getCodec() {
        return this.codec;
    }

    public IGuiElementRenderer<T> getRenderer() {
        return this.renderer.get();
    }

    public GuiElementType<T> setJeiIngredientType(Supplier<Function<T, JEIIngredientRenderer<?, ?>>> renderer) {
        this.jeiRenderer = renderer;
        return this;
    }

    public boolean hasJEIRenderer() {
        return this.jeiRenderer != null;
    }

    public JEIIngredientRenderer<?, ?> getJeiRenderer(T element) {
        return this.jeiRenderer == null ? null : this.jeiRenderer.get().apply(element);
    }
}

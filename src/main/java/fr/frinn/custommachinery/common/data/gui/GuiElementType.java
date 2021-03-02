package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.client.render.element.IGuiElementRenderer;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Supplier;

public class GuiElementType<T extends IGuiElement> extends ForgeRegistryEntry<GuiElementType<? extends IGuiElement>> {

    public static final Codec<GuiElementType<? extends IGuiElement>> CODEC = ResourceLocation.CODEC.xmap(Registration.GUI_ELEMENT_TYPE_REGISTRY::getValue, GuiElementType::getRegistryName);

    private Codec<T> codec;
    private Supplier<IGuiElementRenderer<T>> renderer;

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
}

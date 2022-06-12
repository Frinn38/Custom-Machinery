package fr.frinn.custommachinery.common.guielement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.apiimpl.guielement.AbstractTexturedGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.resources.ResourceLocation;

public class ConfigGuiElement extends AbstractTexturedGuiElement {

    private static final ResourceLocation BASE_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_config.png");

    public static final Codec<ConfigGuiElement> CODEC = RecordCodecBuilder.create(configGuiElement ->
            makeBaseTexturedCodec(configGuiElement, BASE_TEXTURE).apply(configGuiElement, ConfigGuiElement::new)
    );

    public ConfigGuiElement(int x, int y, int width, int height, int priority, ResourceLocation texture) {
        super(x, y, width, height, priority, texture);
    }

    @Override
    public GuiElementType<ConfigGuiElement> getType() {
        return Registration.CONFIG_GUI_ELEMENT.get();
    }
}

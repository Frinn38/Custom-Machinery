package fr.frinn.custommachinery.common.guielement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.codec.CodecLogger;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import net.minecraft.resources.ResourceLocation;

public class ConfigGuiElement extends AbstractTexturedGuiElement {

    private static final ResourceLocation BASE_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_config.png");
    private static final ResourceLocation BASE_TEXTURE_HOVERED = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_config_hovered.png");

    public static final Codec<ConfigGuiElement> CODEC = RecordCodecBuilder.create(configGuiElement ->
            makeBaseTexturedCodec(configGuiElement, BASE_TEXTURE)
                    .and(CodecLogger.loggedOptional(ResourceLocation.CODEC, "texture_hevered", BASE_TEXTURE_HOVERED).forGetter(ConfigGuiElement::getHoveredTexture))
                    .apply(configGuiElement, ConfigGuiElement::new)
    );

    private final ResourceLocation hoveredTexture;

    public ConfigGuiElement(int x, int y, int width, int height, int priority, ResourceLocation texture, ResourceLocation hoveredTexture) {
        super(x, y, width, height, priority, texture);
        this.hoveredTexture = hoveredTexture;
    }

    public ResourceLocation getHoveredTexture() {
        return this.hoveredTexture;
    }

    @Override
    public GuiElementType<ConfigGuiElement> getType() {
        return Registration.CONFIG_GUI_ELEMENT.get();
    }
}

package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import net.minecraft.resources.ResourceLocation;

public class ConfigGuiElement extends AbstractTexturedGuiElement {

    private static final ResourceLocation BASE_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_config.png");
    private static final ResourceLocation BASE_TEXTURE_HOVERED = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_config_hovered.png");

    public static final NamedCodec<ConfigGuiElement> CODEC = NamedCodec.record(configGuiElement ->
            makeBaseTexturedCodec(configGuiElement, BASE_TEXTURE)
                    .and(DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("texture_hevered", BASE_TEXTURE_HOVERED).forGetter(ConfigGuiElement::getHoveredTexture))
                    .apply(configGuiElement, ConfigGuiElement::new), "Config gui element"
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

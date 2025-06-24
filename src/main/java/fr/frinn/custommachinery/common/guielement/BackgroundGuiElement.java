package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElement;
import fr.frinn.custommachinery.impl.util.TextureInfo;
import fr.frinn.custommachinery.impl.util.TextureSizeHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class BackgroundGuiElement extends AbstractGuiElement {

    public static final TextureInfo BASE_BACKGROUND = CustomMachinery.texture("textures/gui/base_background.png");

    public static final NamedCodec<BackgroundGuiElement> CODEC = NamedCodec.record(sizeGuiElement ->
            sizeGuiElement.group(
                    TextureInfo.CODEC.optionalFieldOf("texture", BASE_BACKGROUND).forGetter(BackgroundGuiElement::getTexture),
                    NamedCodec.intRange(1, 3840).optionalFieldOf("width", -1).forGetter(BackgroundGuiElement::getWidth),
                    NamedCodec.intRange(1, 2160).optionalFieldOf("height", -1).forGetter(BackgroundGuiElement::getHeight)
            ).apply(sizeGuiElement, BackgroundGuiElement::new), "Size gui element"
    );

    public BackgroundGuiElement(TextureInfo texture, int width, int height) {
        super(new Properties(0, 0, width, height, 0, texture, null, Collections.emptyList(), ""));
    }

    @Nullable
    public TextureInfo getTexture() {
        return this.getProperties().texture();
    }

    @Override
    public int getWidth() {
        if(super.getWidth() > 0)
            return super.getWidth();
        int width = TextureSizeHelper.getTextureWidth(this.getTexture() == null ? null : this.getTexture().texture());
        return width == 0 ? 256 : width;
    }

    @Override
    public int getHeight() {
        if(super.getHeight() > 0)
            return super.getHeight();
        int height = TextureSizeHelper.getTextureHeight(this.getTexture() == null ? null : this.getTexture().texture());
        return height == 0 ? 256 : height;
    }

    @Override
    public GuiElementType<BackgroundGuiElement> getType() {
        return Registration.BACKGROUND_GUI_ELEMENT.get();
    }
}

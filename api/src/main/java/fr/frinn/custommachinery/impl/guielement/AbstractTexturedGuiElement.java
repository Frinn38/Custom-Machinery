package fr.frinn.custommachinery.impl.guielement;

import com.mojang.datafixers.Products;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.codec.NamedRecordCodec;
import fr.frinn.custommachinery.impl.util.TextureSizeHelper;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractTexturedGuiElement extends AbstractGuiElement {

    private final ResourceLocation texture;

    public AbstractTexturedGuiElement(int x, int y, int width, int height, int priority, ResourceLocation texture) {
        super(x, y, width, height, priority);
        this.texture = texture;
    }

    public ResourceLocation getTexture() {
        return this.texture;
    }

    @Override
    public int getWidth() {
        if(super.getWidth() >= 0)
            return super.getWidth();
        else if(Platform.getEnvironment() == Env.CLIENT)
            return TextureSizeHelper.getTextureWidth(this.texture);
        else
            return -1;
    }

    @Override
    public int getHeight() {
        if(super.getHeight() >= 0)
            return super.getHeight();
        else if(Platform.getEnvironment() == Env.CLIENT)
            return TextureSizeHelper.getTextureHeight(this.texture);
        else
            return -1;
    }

    public static <T extends AbstractTexturedGuiElement> Products.P6<NamedRecordCodec.Mu<T>, Integer, Integer, Integer, Integer, Integer, ResourceLocation> makeBaseTexturedCodec(NamedRecordCodec.Instance<T> texturedGuiElement, ResourceLocation defaultTexture) {
        return makeBaseCodec(texturedGuiElement).and(
                DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("texture", defaultTexture).forGetter(AbstractTexturedGuiElement::getTexture)
        );
    }
}

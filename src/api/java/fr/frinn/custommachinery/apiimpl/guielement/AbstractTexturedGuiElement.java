package fr.frinn.custommachinery.apiimpl.guielement;

import com.mojang.datafixers.Products;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.utils.TextureSizeHelper;
import fr.frinn.custommachinery.apiimpl.codec.CodecLogger;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.DistExecutor;

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
        return super.getWidth() >= 0 ? super.getWidth() : DistExecutor.unsafeRunForDist(() -> () -> TextureSizeHelper.getTextureWidth(this.texture), () -> () -> -1);
    }

    @Override
    public int getHeight() {
        return super.getHeight() >= 0 ? super.getHeight() : DistExecutor.unsafeRunForDist(() -> () -> TextureSizeHelper.getTextureHeight(this.texture), () -> () -> -1);
    }

    public static <T extends AbstractTexturedGuiElement> Products.P6<RecordCodecBuilder.Mu<T>, Integer, Integer, Integer, Integer, Integer, ResourceLocation> makeBaseTexturedCodec(RecordCodecBuilder.Instance<T> texturedGuiElement, ResourceLocation defaultTexture) {
        return makeBaseCodec(texturedGuiElement).and(
                CodecLogger.loggedOptional(ResourceLocation.CODEC,"texture", defaultTexture).forGetter(AbstractTexturedGuiElement::getTexture)
        );
    }
}

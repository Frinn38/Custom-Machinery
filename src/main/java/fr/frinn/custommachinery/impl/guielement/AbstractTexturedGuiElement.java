package fr.frinn.custommachinery.impl.guielement;

import fr.frinn.custommachinery.impl.util.TextureInfo;
import fr.frinn.custommachinery.impl.util.TextureSizeHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;

public abstract class AbstractTexturedGuiElement extends AbstractGuiElement {

    private final TextureInfo texture;

    public AbstractTexturedGuiElement(Properties properties) {
        super(properties);
        if(properties.texture() == null)
            throw new IllegalArgumentException("Can't make a TexturedGuiElement without texture");
        this.texture = properties.texture();
    }

    public AbstractTexturedGuiElement(Properties properties, TextureInfo defaultTexture) {
        super(properties);
        this.texture = defaultTexture;
    }

    public TextureInfo getTexture() {
        return this.texture;
    }

    public TextureInfo getTextureHovered() {
        return this.getProperties().textureHovered();
    }

    @Override
    public int getWidth() {
        if(super.getWidth() >= 0)
            return super.getWidth();
        else if(FMLLoader.getDist() == Dist.CLIENT)
            return TextureSizeHelper.getTextureWidth(this.getTexture().texture());
        else
            return -1;
    }

    @Override
    public int getHeight() {
        if(super.getHeight() >= 0)
            return super.getHeight();
        else if(FMLLoader.getDist() == Dist.CLIENT)
            return TextureSizeHelper.getTextureHeight(this.getTexture().texture());
        else
            return -1;
    }
}

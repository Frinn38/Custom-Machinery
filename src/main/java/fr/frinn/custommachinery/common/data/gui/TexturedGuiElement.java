package fr.frinn.custommachinery.common.data.gui;

import fr.frinn.custommachinery.client.TextureSizeHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.DistExecutor;

public abstract class TexturedGuiElement extends AbstractGuiElement {

    private ResourceLocation texture;

    public TexturedGuiElement(int x, int y, int width, int height, int priority, ResourceLocation texture) {
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
}

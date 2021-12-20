package fr.frinn.custommachinery.apiimpl.guielement;

import fr.frinn.custommachinery.api.utils.TextureSizeHelper;
import net.minecraft.util.ResourceLocation;
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
}

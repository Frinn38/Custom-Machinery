package fr.frinn.custommachinery.common.data.gui;

import fr.frinn.custommachinery.client.TextureSizeHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.DistExecutor;

public abstract class AbstractGuiElement implements IGuiElement {

    private int x;
    private int y;
    private int width;
    private int height;
    private int priority;
    private ResourceLocation baseTexture;

    public AbstractGuiElement(int x, int y, int width, int height, int priority) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.priority = priority;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getWidth() {
        return this.width >= 0 ? this.width : DistExecutor.unsafeRunForDist(() -> () -> TextureSizeHelper.getTextureWidth(this.baseTexture), () -> () -> -1);
    }

    @Override
    public int getHeight() {
        return this.height >= 0 ? this.height : DistExecutor.unsafeRunForDist(() -> () -> TextureSizeHelper.getTextureHeight(this.baseTexture), () -> () -> -1);
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    public void setBaseTexture(ResourceLocation baseTexture) {
        this.baseTexture = baseTexture;
    }
}

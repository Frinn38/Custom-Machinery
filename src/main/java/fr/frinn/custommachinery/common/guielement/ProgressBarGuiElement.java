package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import fr.frinn.custommachinery.impl.util.TextureInfo;
import fr.frinn.custommachinery.impl.util.TextureSizeHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;

public class ProgressBarGuiElement extends AbstractTexturedGuiElement {

    public static final TextureInfo BASE_EMPTY_TEXTURE = CustomMachinery.texture("textures/gui/base_progress_empty.png");
    public static final TextureInfo BASE_FILLED_TEXTURE = CustomMachinery.texture("textures/gui/base_progress_filled.png");

    public static final NamedCodec<ProgressBarGuiElement> CODEC = NamedCodec.record(progressGuiElement ->
            progressGuiElement.group(
                    makePropertiesCodec().forGetter(ProgressBarGuiElement::getProperties),
                    TextureInfo.CODEC.optionalFieldOf("texture_empty", BASE_EMPTY_TEXTURE).forGetter(ProgressBarGuiElement::getEmptyTexture),
                    TextureInfo.CODEC.optionalFieldOf("texture_filled", BASE_FILLED_TEXTURE).forGetter(ProgressBarGuiElement::getFilledTexture),
                    NamedCodec.enumCodec(Orientation.class).optionalFieldOf("orientation", Orientation.RIGHT).aliases("direction").forGetter(ProgressBarGuiElement::getDirection),
                    NamedCodec.FLOAT.optionalFieldOf("start", 0.0F).forGetter(element -> element.start),
                    NamedCodec.FLOAT.optionalFieldOf("end", 1.0F).forGetter(element -> element.end),
                    NamedCodec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("core", 1).forGetter(element -> element.core)
            ).apply(progressGuiElement, ProgressBarGuiElement::new), "Progress bar gui element"
    );

    private final TextureInfo emptyTexture;
    private final TextureInfo filledTexture;
    private final Orientation orientation;
    private final float start;
    private final float end;
    private final int core;

    public ProgressBarGuiElement(Properties properties, TextureInfo emptyTexture, TextureInfo filledTexture, Orientation orientation, float start, float end, int core) {
        super(properties, emptyTexture);
        this.emptyTexture = emptyTexture;
        this.filledTexture = filledTexture;
        this.orientation = orientation;
        this.start = start;
        this.end = end;
        this.core = core;
    }

    @Override
    public GuiElementType<ProgressBarGuiElement> getType() {
        return Registration.PROGRESS_GUI_ELEMENT.get();
    }

    @Override
    public int getWidth() {
        if(this.getProperties().width() >= 0)
            return this.getProperties().width();
        else if(FMLLoader.getDist() == Dist.CLIENT)
            if(this.getTexture().equals(BASE_EMPTY_TEXTURE))
                return switch (this.orientation) {
                    case TOP, BOTTOM -> TextureSizeHelper.getTextureHeight(this.getTexture().texture());
                    default -> TextureSizeHelper.getTextureWidth(this.getTexture().texture());
                };
            else
                return TextureSizeHelper.getTextureWidth(this.getTexture().texture());
        else
            return -1;
    }

    @Override
    public int getHeight() {
        if(this.getProperties().height() >= 0)
            return this.getProperties().height();
        else if(FMLLoader.getDist() == Dist.CLIENT)
            if(this.getTexture().equals(BASE_EMPTY_TEXTURE))
                return switch (this.orientation) {
                    case TOP, BOTTOM -> TextureSizeHelper.getTextureWidth(this.getTexture().texture());
                    default -> TextureSizeHelper.getTextureHeight(this.getTexture().texture());
                };
            else
                return TextureSizeHelper.getTextureHeight(this.getTexture().texture());
        else
            return -1;
    }

    public TextureInfo getEmptyTexture() {
        return this.emptyTexture;
    }

    public TextureInfo getFilledTexture() {
        return this.filledTexture;
    }

    public Orientation getDirection() {
        return this.orientation;
    }

    public float getStart() {
        return this.start;
    }

    public float getEnd() {
        return this.end;
    }

    public int getCore() {
        return this.core - 1;
    }

    public enum Orientation {
        RIGHT,
        LEFT,
        TOP,
        BOTTOM,
        CLOCKWISE,
        COUNTER_CLOCKWISE
    }
}

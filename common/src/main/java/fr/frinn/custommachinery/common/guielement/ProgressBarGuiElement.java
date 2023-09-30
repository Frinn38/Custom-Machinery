package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElement;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import net.minecraft.resources.ResourceLocation;

public class ProgressBarGuiElement extends AbstractTexturedGuiElement {

    public static final ResourceLocation BASE_EMPTY_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_progress_empty.png");
    public static final ResourceLocation BASE_FILLED_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_progress_filled.png");

    public static final NamedCodec<ProgressBarGuiElement> CODEC = NamedCodec.record(progressGuiElement ->
            progressGuiElement.group(
                    makePropertiesCodec().forGetter(ProgressBarGuiElement::getProperties),
                    DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("texture_empty", BASE_EMPTY_TEXTURE).forGetter(ProgressBarGuiElement::getEmptyTexture),
                    DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("texture_filled", BASE_FILLED_TEXTURE).forGetter(ProgressBarGuiElement::getFilledTexture),
                    Codecs.PROGRESS_DIRECTION.optionalFieldOf("direction", Orientation.RIGHT).forGetter(ProgressBarGuiElement::getDirection),
                    NamedCodec.FLOAT.optionalFieldOf("start", 0.0F).forGetter(element -> element.start),
                    NamedCodec.FLOAT.optionalFieldOf("end", 1.0F).forGetter(element -> element.end)
            ).apply(progressGuiElement, ProgressBarGuiElement::new), "Progress bar gui element"
    );

    private final ResourceLocation emptyTexture;
    private final ResourceLocation filledTexture;
    private final Orientation orientation;
    private final float start;
    private final float end;

    public ProgressBarGuiElement(Properties properties, ResourceLocation emptyTexture, ResourceLocation filledTexture, Orientation orientation, float start, float end) {
        super(properties, emptyTexture);
        this.emptyTexture = emptyTexture;
        this.filledTexture = filledTexture;
        this.orientation = orientation;
        this.start = start;
        this.end = end;
    }

    @Override
    public GuiElementType<ProgressBarGuiElement> getType() {
        return Registration.PROGRESS_GUI_ELEMENT.get();
    }

    public ResourceLocation getEmptyTexture() {
        return this.emptyTexture;
    }

    public ResourceLocation getFilledTexture() {
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

    public enum Orientation {
        RIGHT,
        LEFT,
        TOP,
        BOTTOM;
    }
}

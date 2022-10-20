package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.CodecLogger;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.apiimpl.guielement.AbstractTexturedGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import net.minecraft.util.ResourceLocation;

public class ProgressBarGuiElement extends AbstractTexturedGuiElement {

    public static final ResourceLocation BASE_EMPTY_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_progress_empty.png");
    public static final ResourceLocation BASE_FILLED_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_progress_filled.png");

    public static final Codec<ProgressBarGuiElement> CODEC = RecordCodecBuilder.create(progressGuiElement ->
            makeBaseCodec(progressGuiElement).and(
                    progressGuiElement.group(
                            CodecLogger.loggedOptional(ResourceLocation.CODEC,"emptyTexture", BASE_EMPTY_TEXTURE).forGetter(ProgressBarGuiElement::getEmptyTexture),
                            CodecLogger.loggedOptional(ResourceLocation.CODEC,"filledTexture", BASE_FILLED_TEXTURE).forGetter(ProgressBarGuiElement::getFilledTexture),
                            CodecLogger.loggedOptional(Codecs.PROGRESS_DIRECTION, "direction", Direction.RIGHT).forGetter(ProgressBarGuiElement::getDirection)
                    )
            ).apply(progressGuiElement, ProgressBarGuiElement::new)
    );

    private final ResourceLocation emptyTexture;
    private final ResourceLocation filledTexture;
    private final Direction direction;

    public ProgressBarGuiElement(int x, int y, int width, int height, int priority, ResourceLocation emptyTexture, ResourceLocation filledTexture, Direction direction) {
        super(x, y, width, height, priority, emptyTexture);
        this.emptyTexture = emptyTexture;
        this.filledTexture = filledTexture;
        this.direction = direction;
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

    public Direction getDirection() {
        return this.direction;
    }

    public enum Direction {
        RIGHT,
        LEFT,
        TOP,
        BOTTOM;
    }
}

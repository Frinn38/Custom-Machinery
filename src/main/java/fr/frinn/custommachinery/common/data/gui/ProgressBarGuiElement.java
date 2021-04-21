package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;

public class ProgressBarGuiElement extends AbstractGuiElement {

    private static final ResourceLocation BASE_EMPTY_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_progress_empty.png");
    private static final ResourceLocation BASE_FILLED_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_progress_filled.png");

    public static final Codec<ProgressBarGuiElement> CODEC = RecordCodecBuilder.create(progressGuiElementCodec ->
            progressGuiElementCodec.group(
                    Codec.INT.fieldOf("x").forGetter(ProgressBarGuiElement::getX),
                    Codec.INT.fieldOf("y").forGetter(ProgressBarGuiElement::getY),
                    Codec.INT.optionalFieldOf("width", -1).forGetter(AbstractGuiElement::getWidth),
                    Codec.INT.optionalFieldOf("height", -1).forGetter(AbstractGuiElement::getHeight),
                    Codec.INT.optionalFieldOf("priority", 0).forGetter(AbstractGuiElement::getPriority),
                    ResourceLocation.CODEC.optionalFieldOf("emptyTexture", BASE_EMPTY_TEXTURE).forGetter(ProgressBarGuiElement::getEmptyTexture),
                    ResourceLocation.CODEC.optionalFieldOf("filledTexture", BASE_FILLED_TEXTURE).forGetter(ProgressBarGuiElement::getFilledTexture)
            ).apply(progressGuiElementCodec, ProgressBarGuiElement::new)
    );

    private ResourceLocation emptyTexture;
    private ResourceLocation filledTexture;

    public ProgressBarGuiElement(int x, int y, int width, int height, int priority, ResourceLocation emptyTexture, ResourceLocation filledTexture) {
        super(x, y, width, height, priority);
        this.emptyTexture = emptyTexture;
        this.filledTexture = filledTexture;
        setBaseTexture(emptyTexture);
    }

    @Override
    public GuiElementType getType() {
        return Registration.PROGRESS_GUI_ELEMENT.get();
    }

    public ResourceLocation getEmptyTexture() {
        return this.emptyTexture;
    }

    public ResourceLocation getFilledTexture() {
        return this.filledTexture;
    }
}

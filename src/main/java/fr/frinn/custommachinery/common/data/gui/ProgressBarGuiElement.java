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
                    Codec.INT.optionalFieldOf("width").forGetter(element -> Optional.of(element.getWidth())),
                    Codec.INT.optionalFieldOf("height").forGetter(element -> Optional.of(element.getHeight())),
                    Codec.INT.optionalFieldOf("priority").forGetter(element -> Optional.of(element.getPriority())),
                    ResourceLocation.CODEC.optionalFieldOf("emptyTexture").forGetter(gui -> Optional.of(gui.getEmptyTexture())),
                    ResourceLocation.CODEC.optionalFieldOf("filledTexture").forGetter(gui -> Optional.of(gui.getFilledTexture()))
            ).apply(progressGuiElementCodec, (x, y, width, height, priority, empty, filled) ->
                    new ProgressBarGuiElement(x, y, width.orElse(-1), height.orElse(-1), priority.orElse(0), empty.orElse(BASE_EMPTY_TEXTURE), filled.orElse(BASE_FILLED_TEXTURE))
            )
    );

    private ResourceLocation emptyTexture;
    private ResourceLocation filledTexture;

    public ProgressBarGuiElement(int x, int y, int width, int height, int priority, ResourceLocation emptyTexture, ResourceLocation filledTexture) {
        super(x, y, width, height, priority);
        this.emptyTexture = emptyTexture;
        this.filledTexture = filledTexture;
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

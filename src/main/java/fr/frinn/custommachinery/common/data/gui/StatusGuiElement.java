package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;

public class StatusGuiElement extends AbstractGuiElement {

    private static final ResourceLocation BASE_STATUS_IDLE_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_status_idle.png");
    private static final ResourceLocation BASE_STATUS_RUNNING_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_status_running.png");
    private static final ResourceLocation BASE_STATUS_ERRORED_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_status_errored.png");

    public static final Codec<StatusGuiElement> CODEC = RecordCodecBuilder.create(statusGuiElementInstance ->
            statusGuiElementInstance.group(
                    Codec.INT.fieldOf("x").forGetter(StatusGuiElement::getX),
                    Codec.INT.fieldOf("y").forGetter(StatusGuiElement::getY),
                    Codec.INT.optionalFieldOf("width", -1).forGetter(AbstractGuiElement::getWidth),
                    Codec.INT.optionalFieldOf("height", -1).forGetter(AbstractGuiElement::getHeight),
                    Codec.INT.optionalFieldOf("priority", 0).forGetter(AbstractGuiElement::getPriority),
                    ResourceLocation.CODEC.optionalFieldOf("idleTexture", BASE_STATUS_IDLE_TEXTURE).forGetter(StatusGuiElement::getIdleTexture),
                    ResourceLocation.CODEC.optionalFieldOf("runningTexture", BASE_STATUS_RUNNING_TEXTURE).forGetter(StatusGuiElement::getRunningTexture),
                    ResourceLocation.CODEC.optionalFieldOf("erroredTexture", BASE_STATUS_ERRORED_TEXTURE).forGetter(StatusGuiElement::getErroredTexture)
            ).apply(statusGuiElementInstance, StatusGuiElement::new)
    );

    private ResourceLocation idleTexture;
    private ResourceLocation runningTexture;
    private ResourceLocation erroredTexture;

    public StatusGuiElement(int x, int y, int width, int height, int priority, ResourceLocation idleTexture, ResourceLocation runningTexture, ResourceLocation erroredTexture) {
        super(x, y, width, height, priority);
        this.idleTexture = idleTexture;
        this.runningTexture = runningTexture;
        this.erroredTexture = erroredTexture;
        setBaseTexture(idleTexture);
    }

    @Override
    public GuiElementType getType() {
        return Registration.STATUS_GUI_ELEMENT.get();
    }

    public ResourceLocation getIdleTexture() {
        return this.idleTexture;
    }

    public ResourceLocation getRunningTexture() {
        return this.runningTexture;
    }

    public ResourceLocation getErroredTexture() {
        return this.erroredTexture;
    }
}

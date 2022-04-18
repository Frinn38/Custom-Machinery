package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.CodecLogger;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.apiimpl.guielement.AbstractGuiElement;
import fr.frinn.custommachinery.apiimpl.guielement.AbstractTexturedGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.ResourceLocation;

public class StatusGuiElement extends AbstractTexturedGuiElement {

    private static final ResourceLocation BASE_STATUS_IDLE_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_status_idle.png");
    private static final ResourceLocation BASE_STATUS_RUNNING_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_status_running.png");
    private static final ResourceLocation BASE_STATUS_ERRORED_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_status_errored.png");

    public static final Codec<StatusGuiElement> CODEC = RecordCodecBuilder.create(statusGuiElement ->
            makeBaseCodec(statusGuiElement).and(
                    statusGuiElement.group(
                            CodecLogger.loggedOptional(ResourceLocation.CODEC,"idleTexture", BASE_STATUS_IDLE_TEXTURE).forGetter(StatusGuiElement::getIdleTexture),
                            CodecLogger.loggedOptional(ResourceLocation.CODEC,"runningTexture", BASE_STATUS_RUNNING_TEXTURE).forGetter(StatusGuiElement::getRunningTexture),
                            CodecLogger.loggedOptional(ResourceLocation.CODEC,"erroredTexture", BASE_STATUS_ERRORED_TEXTURE).forGetter(StatusGuiElement::getErroredTexture)
                    )
            ).apply(statusGuiElement, StatusGuiElement::new)
    );

    private final ResourceLocation idleTexture;
    private final ResourceLocation runningTexture;
    private final ResourceLocation erroredTexture;

    public StatusGuiElement(int x, int y, int width, int height, int priority, ResourceLocation idleTexture, ResourceLocation runningTexture, ResourceLocation erroredTexture) {
        super(x, y, width, height, priority, idleTexture);
        this.idleTexture = idleTexture;
        this.runningTexture = runningTexture;
        this.erroredTexture = erroredTexture;
    }

    @Override
    public GuiElementType<StatusGuiElement> getType() {
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

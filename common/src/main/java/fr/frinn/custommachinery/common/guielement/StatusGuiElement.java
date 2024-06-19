package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import net.minecraft.resources.ResourceLocation;

public class StatusGuiElement extends AbstractTexturedGuiElement {

    public static final ResourceLocation BASE_STATUS_IDLE_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_status_idle.png");
    public static final ResourceLocation BASE_STATUS_RUNNING_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_status_running.png");
    public static final ResourceLocation BASE_STATUS_ERRORED_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_status_errored.png");

    public static final NamedCodec<StatusGuiElement> CODEC = NamedCodec.record(statusGuiElement ->
            statusGuiElement.group(
                    makePropertiesCodec().forGetter(StatusGuiElement::getProperties),
                    DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("texture_idle", BASE_STATUS_IDLE_TEXTURE).forGetter(StatusGuiElement::getIdleTexture),
                    DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("texture_running", BASE_STATUS_RUNNING_TEXTURE).forGetter(StatusGuiElement::getRunningTexture),
                    DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("texture_errored", BASE_STATUS_ERRORED_TEXTURE).forGetter(StatusGuiElement::getErroredTexture)
            ).apply(statusGuiElement, StatusGuiElement::new), "Status gui element"
    );

    private final ResourceLocation idleTexture;
    private final ResourceLocation runningTexture;
    private final ResourceLocation erroredTexture;

    public StatusGuiElement(Properties properties, ResourceLocation idleTexture, ResourceLocation runningTexture, ResourceLocation erroredTexture) {
        super(properties, idleTexture);
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

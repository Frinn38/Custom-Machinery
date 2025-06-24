package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import fr.frinn.custommachinery.impl.util.TextureInfo;

public class StatusGuiElement extends AbstractTexturedGuiElement {

    public static final TextureInfo BASE_STATUS_IDLE_TEXTURE = CustomMachinery.texture("textures/gui/base_status_idle.png");
    public static final TextureInfo BASE_STATUS_RUNNING_TEXTURE = CustomMachinery.texture("textures/gui/base_status_running.png");
    public static final TextureInfo BASE_STATUS_ERRORED_TEXTURE = CustomMachinery.texture("textures/gui/base_status_errored.png");

    public static final NamedCodec<StatusGuiElement> CODEC = NamedCodec.record(statusGuiElement ->
            statusGuiElement.group(
                    makePropertiesCodec().forGetter(StatusGuiElement::getProperties),
                    TextureInfo.CODEC.optionalFieldOf("texture_idle", BASE_STATUS_IDLE_TEXTURE).forGetter(StatusGuiElement::getIdleTexture),
                    TextureInfo.CODEC.optionalFieldOf("texture_running", BASE_STATUS_RUNNING_TEXTURE).forGetter(StatusGuiElement::getRunningTexture),
                    TextureInfo.CODEC.optionalFieldOf("texture_errored", BASE_STATUS_ERRORED_TEXTURE).forGetter(StatusGuiElement::getErroredTexture)
            ).apply(statusGuiElement, StatusGuiElement::new), "Status gui element"
    );

    private final TextureInfo idleTexture;
    private final TextureInfo runningTexture;
    private final TextureInfo erroredTexture;

    public StatusGuiElement(Properties properties, TextureInfo idleTexture, TextureInfo runningTexture, TextureInfo erroredTexture) {
        super(properties, idleTexture);
        this.idleTexture = idleTexture;
        this.runningTexture = runningTexture;
        this.erroredTexture = erroredTexture;
    }

    @Override
    public GuiElementType<StatusGuiElement> getType() {
        return Registration.STATUS_GUI_ELEMENT.get();
    }

    public TextureInfo getIdleTexture() {
        return this.idleTexture;
    }

    public TextureInfo getRunningTexture() {
        return this.runningTexture;
    }

    public TextureInfo getErroredTexture() {
        return this.erroredTexture;
    }
}

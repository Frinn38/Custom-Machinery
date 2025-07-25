package fr.frinn.custommachinery.client.screen.creation.gui.builder;

import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiElementBuilderPopup;
import fr.frinn.custommachinery.client.screen.creation.gui.IGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.MutableProperties;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.client.screen.widget.IntegerSlider;
import fr.frinn.custommachinery.common.guielement.StatusGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElement.Properties;
import fr.frinn.custommachinery.impl.util.TextureInfo;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class StatusGuiElementBuilder implements IGuiElementBuilder<StatusGuiElement> {

    @Override
    public GuiElementType<StatusGuiElement> type() {
        return Registration.STATUS_GUI_ELEMENT.get();
    }

    @Override
    public StatusGuiElement make(Properties properties, @Nullable StatusGuiElement from) {
        if(from != null)
            return new StatusGuiElement(properties, from.getIdleTexture(), from.getRunningTexture(), from.getErroredTexture(), from.getCore());
        else
            return new StatusGuiElement(properties, StatusGuiElement.BASE_STATUS_IDLE_TEXTURE, StatusGuiElement.BASE_STATUS_RUNNING_TEXTURE, StatusGuiElement.BASE_STATUS_ERRORED_TEXTURE, 0);

    }

    @Override
    public PopupScreen makeConfigPopup(MachineEditScreen parent, MutableProperties properties, @Nullable StatusGuiElement from, Consumer<StatusGuiElement> onFinish) {
        return new StatusGuiElementBuilderPopup(parent, properties, from, onFinish);
    }

    public static class StatusGuiElementBuilderPopup extends GuiElementBuilderPopup<StatusGuiElement> {

        private TextureInfo idleTexture = StatusGuiElement.BASE_STATUS_IDLE_TEXTURE;
        private TextureInfo runningTexture = StatusGuiElement.BASE_STATUS_RUNNING_TEXTURE;
        private TextureInfo erroredTexture = StatusGuiElement.BASE_STATUS_ERRORED_TEXTURE;
        private int core = 0;

        public StatusGuiElementBuilderPopup(BaseScreen parent, MutableProperties properties, @Nullable StatusGuiElement from, Consumer<StatusGuiElement> onFinish) {
            super(parent, properties, from, onFinish);
            if(from != null) {
                this.idleTexture = from.getIdleTexture();
                this.runningTexture = from.getRunningTexture();
                this.erroredTexture = from.getErroredTexture();
                this.core = from.getCore();
            }
        }

        @Override
        public StatusGuiElement makeElement() {
            return new StatusGuiElement(this.properties.build(), this.idleTexture, this.runningTexture, this.erroredTexture, this.core);
        }

        @Override
        public void addWidgets(RowHelper row) {
            this.addTexture(row, Component.translatable("custommachinery.gui.creation.gui.status.idle"), texture -> this.idleTexture = texture, this.idleTexture);
            this.addTexture(row, Component.translatable("custommachinery.gui.creation.gui.status.running"), texture -> this.runningTexture = texture, this.runningTexture);
            this.addTexture(row, Component.translatable("custommachinery.gui.creation.gui.status.errored"), texture -> this.erroredTexture = texture, this.erroredTexture);
            this.addPriority(row);
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.progress.core"), this.font));
            row.addChild(IntegerSlider.builder().bounds(0, 16).defaultValue(this.core).displayOnlyValue().setResponder(value -> this.core = value).create(0, 0, 100, 20, Component.empty()));
        }
    }
}

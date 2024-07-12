package fr.frinn.custommachinery.client.screen.creation.gui.builder;

import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiElementBuilderPopup;
import fr.frinn.custommachinery.client.screen.creation.gui.IGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.MutableProperties;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.client.screen.widget.DoubleSlider;
import fr.frinn.custommachinery.client.screen.widget.IntegerSlider;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement.Orientation;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElement.Properties;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ProgressBarGuiElementBuilder implements IGuiElementBuilder<ProgressBarGuiElement> {

    @Override
    public GuiElementType<ProgressBarGuiElement> type() {
        return Registration.PROGRESS_GUI_ELEMENT.get();
    }

    @Override
    public ProgressBarGuiElement make(Properties properties, @Nullable ProgressBarGuiElement from) {
        if(from != null)
            return new ProgressBarGuiElement(properties, from.getEmptyTexture(), from.getFilledTexture(), from.getDirection(), from.getStart(), from.getEnd(), from.getCore());
        else
            return new ProgressBarGuiElement(properties, ProgressBarGuiElement.BASE_EMPTY_TEXTURE, ProgressBarGuiElement.BASE_FILLED_TEXTURE, Orientation.RIGHT, 0.0F, 0.0F, 0);
    }

    @Override
    public PopupScreen makeConfigPopup(MachineEditScreen parent, MutableProperties properties, @Nullable ProgressBarGuiElement from, Consumer<ProgressBarGuiElement> onFinish) {
        return new ProgressBarGuiElementBuilderPopup(parent, properties, from, onFinish);
    }

    public static class ProgressBarGuiElementBuilderPopup extends GuiElementBuilderPopup<ProgressBarGuiElement> {

        private ResourceLocation emptyTexture = ProgressBarGuiElement.BASE_EMPTY_TEXTURE;
        private ResourceLocation filledTexture = ProgressBarGuiElement.BASE_FILLED_TEXTURE;
        private CycleButton<Orientation> orientation;
        private float start = 0.0F;
        private float end = 1.0F;
        private int core = 0;

        public ProgressBarGuiElementBuilderPopup(BaseScreen parent, MutableProperties properties, @Nullable ProgressBarGuiElement from, Consumer<ProgressBarGuiElement> onFinish) {
            super(parent, properties, from, onFinish);
            if(from != null) {
                this.emptyTexture = from.getEmptyTexture();
                this.filledTexture = from.getFilledTexture();
                this.start = from.getStart();
                this.end = from.getEnd();
                this.core = from.getCore();
            }
        }

        @Override
        public ProgressBarGuiElement makeElement() {
            return new ProgressBarGuiElement(this.properties.build(), this.emptyTexture, this.filledTexture, this.orientation.getValue(), this.start, this.end, this.core);
        }

        @Override
        public void addWidgets(RowHelper row) {
            this.addTexture(row, Component.translatable("custommachinery.gui.creation.gui.empty"), texture -> this.emptyTexture = texture, this.emptyTexture);
            this.addTexture(row, Component.translatable("custommachinery.gui.creation.gui.filled"), texture -> this.filledTexture = texture, this.filledTexture);
            this.addPriority(row);
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.progress.orientation"), this.font));
            this.orientation = row.addChild(CycleButton.<Orientation>builder(orientation -> Component.literal(orientation.toString())).withValues(Orientation.values()).withInitialValue(this.baseElement == null ? Orientation.RIGHT : this.baseElement.getDirection()).displayOnlyValue().create(0, 0, 100, 20, Component.translatable("custommachinery.gui.creation.gui.progress.orientation")));
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.progress.start"), this.font));
            row.addChild(DoubleSlider.builder().bounds(-1, 1).defaultValue(this.start).displayOnlyValue().setResponder(value -> this.start = value.floatValue()).create(0, 0, 100, 20, Component.empty()));
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.progress.end"), this.font));
            row.addChild(DoubleSlider.builder().bounds(0, 2).defaultValue(this.end).displayOnlyValue().setResponder(value -> this.end = value.floatValue()).create(0, 0, 100, 20, Component.empty()));
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.progress.core"), this.font));
            row.addChild(IntegerSlider.builder().bounds(1, 16).defaultValue(this.core).displayOnlyValue().setResponder(value -> this.core = value - 1).create(0, 0, 100, 20, Component.empty()));
        }
    }
}

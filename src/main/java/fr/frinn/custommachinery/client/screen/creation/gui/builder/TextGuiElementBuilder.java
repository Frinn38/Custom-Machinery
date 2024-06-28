package fr.frinn.custommachinery.client.screen.creation.gui.builder;

import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiElementBuilderPopup;
import fr.frinn.custommachinery.client.screen.creation.gui.IGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.MutableProperties;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.client.screen.widget.ComponentEditBox;
import fr.frinn.custommachinery.common.guielement.TextGuiElement;
import fr.frinn.custommachinery.common.guielement.TextGuiElement.Alignment;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElement.Properties;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class TextGuiElementBuilder implements IGuiElementBuilder<TextGuiElement> {

    @Override
    public GuiElementType<TextGuiElement> type() {
        return Registration.TEXT_GUI_ELEMENT.get();
    }

    @Override
    public TextGuiElement make(Properties properties, @Nullable TextGuiElement from) {
        if(from != null)
            return new TextGuiElement(properties, from.getText(), from.getAlignment(), from.showInJei());
        else
            return new TextGuiElement(properties, Component.translatable("custommachinery.gui.creation.gui.text.default"), Alignment.LEFT, false);
    }

    @Override
    public PopupScreen makeConfigPopup(MachineEditScreen parent, MutableProperties properties, @Nullable TextGuiElement from, Consumer<TextGuiElement> onFinish) {
        return new TextGuiElementBuilderPopup(parent, properties, from, onFinish);
    }

    public static class TextGuiElementBuilderPopup extends GuiElementBuilderPopup<TextGuiElement> {

        private ComponentEditBox text;
        private CycleButton<Alignment> alignment;
        private Checkbox showInJei;

        public TextGuiElementBuilderPopup(BaseScreen parent, MutableProperties properties, @Nullable TextGuiElement from, Consumer<TextGuiElement> onFinish) {
            super(parent, properties, from, onFinish);
        }

        @Override
        public TextGuiElement makeElement() {
            return new TextGuiElement(this.properties.build(), this.text.getComponent(), this.alignment.getValue(), this.showInJei.selected());
        }

        @Override
        public void addWidgets(RowHelper row) {
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.text.text"), this.font));
            this.text = row.addChild(new ComponentEditBox(0, 0, 100, 20, Component.translatable("custommachinery.gui.creation.gui.text.text")));
            if(this.baseElement != null)
                this.text.setComponent(this.baseElement.getText());
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.text.alignment"), this.font));
            this.alignment = row.addChild(CycleButton.<Alignment>builder(alignment -> Component.literal(alignment.toString())).withValues(Alignment.values()).withInitialValue(this.baseElement == null ? Alignment.LEFT : this.baseElement.getAlignment()).displayOnlyValue().create(0, 0, 100, 20, Component.translatable("custommachinery.gui.creation.gui.text.alignment")));
            this.addPriority(row);
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.text.jei"), this.font));
            this.showInJei = row.addChild(Checkbox.builder(Component.translatable("custommachinery.gui.creation.gui.text.jei"), this.font).selected(this.baseElement != null && this.baseElement.showInJei()).build());
        }
    }
}

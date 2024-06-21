package fr.frinn.custommachinery.client.screen.creation.gui.builder;

import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiElementBuilderPopup;
import fr.frinn.custommachinery.client.screen.creation.gui.IGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.MutableProperties;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.common.guielement.FluidGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElement.Properties;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class FluidGuiElementBuilder implements IGuiElementBuilder<FluidGuiElement> {

    @Override
    public GuiElementType<FluidGuiElement> type() {
        return Registration.FLUID_GUI_ELEMENT.get();
    }

    @Override
    public FluidGuiElement make(Properties properties, @Nullable FluidGuiElement from) {
        if(from != null)
            return new FluidGuiElement(properties, properties.id(), from.highlight());
        else
            return new FluidGuiElement(properties, "", true);
    }

    @Override
    public PopupScreen makeConfigPopup(MachineEditScreen parent, MutableProperties properties, @Nullable FluidGuiElement from, Consumer<FluidGuiElement> onFinish) {
        return new FluidGuiElementBuilderPopup(parent, properties, from, onFinish);
    }

    public static class FluidGuiElementBuilderPopup extends GuiElementBuilderPopup<FluidGuiElement> {

        private Checkbox highlight;

        public FluidGuiElementBuilderPopup(BaseScreen parent, MutableProperties properties, @Nullable FluidGuiElement from, Consumer<FluidGuiElement> onFinish) {
            super(parent, properties, from, onFinish);
        }

        @Override
        public FluidGuiElement makeElement() {
            return new FluidGuiElement(this.properties.build(), this.properties.getId(), this.highlight.selected());
        }

        @Override
        public Component canCreate() {
            if(this.properties.getId().isEmpty())
                return Component.translatable("custommachinery.gui.creation.gui.id.missing");
            return super.canCreate();
        }

        @Override
        public void addWidgets(RowHelper row) {
            this.addTexture(row, Component.translatable("custommachinery.gui.creation.gui.texture"), this.properties::setTexture, this.baseElement != null ? this.baseElement.getTexture() : FluidGuiElement.BASE_TEXTURE);
            this.addId(row);
            this.addPriority(row);
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.highlight"), this.font));
            this.highlight = row.addChild(Checkbox.builder(Component.translatable("custommachinery.gui.creation.gui.highlight"), this.font).selected(this.baseElement == null || this.baseElement.highlight()).build());
        }
    }
}

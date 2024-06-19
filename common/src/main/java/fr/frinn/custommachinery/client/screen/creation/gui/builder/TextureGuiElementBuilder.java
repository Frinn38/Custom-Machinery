package fr.frinn.custommachinery.client.screen.creation.gui.builder;

import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiElementBuilderPopup;
import fr.frinn.custommachinery.client.screen.creation.gui.IGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.MutableProperties;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.common.guielement.TextureGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElement.Properties;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class TextureGuiElementBuilder implements IGuiElementBuilder<TextureGuiElement> {

    @Override
    public GuiElementType<TextureGuiElement> type() {
        return Registration.TEXTURE_GUI_ELEMENT.get();
    }

    @Override
    public TextureGuiElement make(Properties properties, @Nullable TextureGuiElement from) {
        if(from != null)
            return new TextureGuiElement(properties, from.showInJei());
        else
            return new TextureGuiElement(properties, false);
    }

    @Override
    public PopupScreen makeConfigPopup(MachineEditScreen parent, MutableProperties properties, @Nullable TextureGuiElement from, Consumer<TextureGuiElement> onFinish) {
        return new TextureGuiElementBuilderPopup(parent, properties, from, onFinish);
    }

    public static class TextureGuiElementBuilderPopup extends GuiElementBuilderPopup<TextureGuiElement> {

        private Checkbox jei;

        public TextureGuiElementBuilderPopup(BaseScreen parent, MutableProperties properties, @Nullable TextureGuiElement from, Consumer<TextureGuiElement> onFinish) {
            super(parent, properties, from, onFinish);
        }

        @Override
        public TextureGuiElement makeElement() {
            return new TextureGuiElement(this.properties.build(), this.jei.selected());
        }

        @Override
        public void addWidgets(RowHelper row) {
            this.addTexture(row, Component.translatable("custommachinery.gui.creation.gui.texture"), this.properties::setTexture, this.properties.getTexture());
            this.addTexture(row, Component.translatable("custommachinery.gui.creation.gui.texture_hovered"), this.properties::setTextureHovered, this.properties.getTextureHovered());
            this.addPriority(row);
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.text.jei"), this.font));
            this.jei = row.addChild(new Checkbox(0, 0, 20, 20, Component.translatable("custommachinery.gui.creation.gui.text.jei"), this.baseElement != null && this.baseElement.showInJei()));
        }
    }
}

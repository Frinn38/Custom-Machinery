package fr.frinn.custommachinery.client.screen.creation.gui.builder;

import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiElementBuilderPopup;
import fr.frinn.custommachinery.client.screen.creation.gui.IGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.MutableProperties;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.client.screen.widget.IntegerSlider;
import fr.frinn.custommachinery.common.guielement.TextureGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElement.Properties;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
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
            return new TextureGuiElement(properties, from.showInJei(), from.getZLevel());
        else
            return new TextureGuiElement(properties, false, 0);
    }

    @Override
    public PopupScreen makeConfigPopup(MachineEditScreen parent, MutableProperties properties, @Nullable TextureGuiElement from, Consumer<TextureGuiElement> onFinish) {
        return new TextureGuiElementBuilderPopup(parent, properties, from, onFinish);
    }

    public static class TextureGuiElementBuilderPopup extends GuiElementBuilderPopup<TextureGuiElement> {

        private Checkbox jei;
        private IntegerSlider zLevel;

        public TextureGuiElementBuilderPopup(BaseScreen parent, MutableProperties properties, @Nullable TextureGuiElement from, Consumer<TextureGuiElement> onFinish) {
            super(parent, properties, from, onFinish);
        }

        @Override
        public TextureGuiElement makeElement() {
            return new TextureGuiElement(this.properties.build(), this.jei.selected(), this.zLevel.intValue());
        }

        @Override
        public void addWidgets(RowHelper row) {
            this.addTexture(row, Component.translatable("custommachinery.gui.creation.gui.texture"), this.properties::setTexture, this.properties.getTexture());
            this.addTexture(row, Component.translatable("custommachinery.gui.creation.gui.texture_hovered"), this.properties::setTextureHovered, this.properties.getTextureHovered());
            this.addPriority(row);
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.text.jei"), this.font));
            this.jei = row.addChild(Checkbox.builder(Component.translatable("custommachinery.gui.creation.gui.text.jei"), this.font).selected(this.baseElement != null && this.baseElement.showInJei()).build());
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.texture.zlevel"), this.font));
            this.zLevel = row.addChild(IntegerSlider.builder().bounds(-1000, 1000).defaultValue(this.baseElement != null ? this.baseElement.getZLevel() : 0).displayOnlyValue().create(0, 0, 100, 20, Component.empty()));
            this.zLevel.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.texture.zlevel.tooltip")));
        }
    }
}

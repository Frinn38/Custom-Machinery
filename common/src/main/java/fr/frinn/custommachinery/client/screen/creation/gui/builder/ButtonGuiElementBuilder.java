package fr.frinn.custommachinery.client.screen.creation.gui.builder;

import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiElementBuilderPopup;
import fr.frinn.custommachinery.client.screen.creation.gui.IGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.MutableProperties;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.client.screen.widget.ComponentEditBox;
import fr.frinn.custommachinery.client.screen.widget.IntegerSlider;
import fr.frinn.custommachinery.client.screen.widget.SuggestedEditBox;
import fr.frinn.custommachinery.common.guielement.ButtonGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElement.Properties;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ButtonGuiElementBuilder implements IGuiElementBuilder<ButtonGuiElement> {

    @Override
    public GuiElementType<ButtonGuiElement> type() {
        return Registration.BUTTON_GUI_ELEMENT.get();
    }

    @Override
    public ButtonGuiElement make(Properties properties, @Nullable ButtonGuiElement from) {
        if(from != null)
            return new ButtonGuiElement(properties, from.getTextureToggle(), from.getTextureToggleHovered(), from.isToggle(), from.getText(), from.getItem(), from.getHoldTime());
        else
            return new ButtonGuiElement(properties, ButtonGuiElement.BASE_TEXTURE_TOGGLE, ButtonGuiElement.BASE_TEXTURE_TOGGLE_HOVERED, false, Component.literal(""), ItemStack.EMPTY, 1);
    }

    @Override
    public PopupScreen makeConfigPopup(MachineEditScreen parent, MutableProperties properties, @Nullable ButtonGuiElement from, Consumer<ButtonGuiElement> onFinish) {
        return new ButtonGuiElementBuilderPopup(parent, properties, from, onFinish);
    }

    public static class ButtonGuiElementBuilderPopup extends GuiElementBuilderPopup<ButtonGuiElement> {

        private ResourceLocation textureToggle = ButtonGuiElement.BASE_TEXTURE_TOGGLE;
        private ResourceLocation textureToggleHovered = ButtonGuiElement.BASE_TEXTURE_TOGGLE_HOVERED;
        private Checkbox toggle;
        private ComponentEditBox text;
        private SuggestedEditBox item;
        private IntegerSlider holdTime;

        public ButtonGuiElementBuilderPopup(BaseScreen parent, MutableProperties properties, @Nullable ButtonGuiElement from, Consumer<ButtonGuiElement> onFinish) {
            super(parent, properties, from, onFinish);
            if(from != null) {
                this.textureToggle = from.getTextureToggle();
                this.textureToggleHovered = from.getTextureToggleHovered();
            }
        }

        private ItemStack getItem() {
            try {
                return BuiltInRegistries.ITEM.get(new ResourceLocation(this.item.getValue())).getDefaultInstance();
            } catch (ResourceLocationException | NullPointerException e) {
                return ItemStack.EMPTY;
            }
        }

        @Override
        public ButtonGuiElement makeElement() {
            return new ButtonGuiElement(this.properties.build(), this.textureToggle, this.textureToggleHovered, this.toggle.selected(), this.text.getComponent(), this.getItem(), this.holdTime.intValue());
        }

        @Override
        public Component canCreate() {
            if(this.properties.getId().isEmpty())
                return Component.translatable("custommachinery.gui.creation.gui.id.missing");
            return super.canCreate();
        }

        @Override
        public void addWidgets(RowHelper row) {
            this.addTexture(row, Component.translatable("custommachinery.gui.creation.gui.texture"), this.properties::setTexture, this.baseElement != null ? this.baseElement.getTexture() : ButtonGuiElement.BASE_TEXTURE);
            this.addTexture(row, Component.translatable("custommachinery.gui.creation.gui.texture_hovered"), this.properties::setTextureHovered, this.baseElement != null ? this.baseElement.getTextureHovered() : ButtonGuiElement.BASE_TEXTURE_HOVERED);
            this.addTexture(row, Component.translatable("custommachinery.gui.creation.gui.button.texture_toggle"), texture -> this.textureToggle = texture, this.textureToggle);
            this.addTexture(row, Component.translatable("custommachinery.gui.creation.gui.button.texture_toggle_hovered"), texture -> this.textureToggleHovered = texture, this.textureToggleHovered);
            this.addId(row);
            this.addPriority(row);
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.button.toggle"), this.font));
            this.toggle = row.addChild(new Checkbox(0, 0, 20, 20, Component.translatable("custommachinery.gui.creation.gui.button.toggle"), this.baseElement != null && this.baseElement.isToggle()));
            this.toggle.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.button.toggle.tooltip")));
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.button.text"), this.font));
            this.text = row.addChild(new ComponentEditBox(this.font, 0, 0, 100, 20, Component.translatable("custommachinery.gui.creation.gui.button.text")));
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.button.item"), this.font));
            this.item = row.addChild(new SuggestedEditBox(this.font, 0, 0, 100, 20, Component.translatable("custommachinery.gui.creation.gui.button.item"), 5));
            this.item.setFilter(s -> ResourceLocation.tryParse(s) != null);
            this.item.addSuggestions(BuiltInRegistries.ITEM.keySet().stream().map(ResourceLocation::toString).toList());
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.button.hold_time"), this.font));
            this.holdTime = row.addChild(IntegerSlider.builder().bounds(1, 40).defaultValue(1).displayOnlyValue().create(0, 0, 100, 20, Component.translatable("custommachinery.gui.creation.gui.button.hold_time")));
            this.holdTime.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.button.hold_time.tooltip")));
            if(this.baseElement != null) {
                this.text.setComponent(this.baseElement.getText());
                this.item.setValue(BuiltInRegistries.ITEM.getKey(this.baseElement.getItem().getItem()).toString());
                this.holdTime.setValue(this.baseElement.getHoldTime());
            }
        }
    }
}

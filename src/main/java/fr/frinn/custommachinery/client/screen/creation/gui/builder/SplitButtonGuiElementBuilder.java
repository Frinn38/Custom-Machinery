package fr.frinn.custommachinery.client.screen.creation.gui.builder;

import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiElementBuilderPopup;
import fr.frinn.custommachinery.client.screen.creation.gui.IGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.MutableProperties;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.common.guielement.ButtonGuiElement;
import fr.frinn.custommachinery.common.guielement.SplitButtonGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElement.Properties;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class SplitButtonGuiElementBuilder implements IGuiElementBuilder<SplitButtonGuiElement> {

    @Override
    public GuiElementType<SplitButtonGuiElement> type() {
        return Registration.SPLIT_GUI_ELEMENT.get();
    }

    @Override
    public SplitButtonGuiElement make(Properties properties, @Nullable SplitButtonGuiElement from) {
        if(from != null)
            return new SplitButtonGuiElement(properties, from.getTextureToggle(), from.getTextureToggleHovered(), from.getSlots());
        else
            return new SplitButtonGuiElement(properties, ButtonGuiElement.BASE_TEXTURE_TOGGLE, ButtonGuiElement.BASE_TEXTURE_TOGGLE_HOVERED, Collections.emptyList());
    }

    @Override
    public PopupScreen makeConfigPopup(MachineEditScreen parent, MutableProperties properties, @Nullable SplitButtonGuiElement from, Consumer<SplitButtonGuiElement> onFinish) {
        return new SplitButtonGuiElementBuilderPopup(parent, properties, from, onFinish);
    }

    public static class SplitButtonGuiElementBuilderPopup extends GuiElementBuilderPopup<SplitButtonGuiElement> {

        private ResourceLocation textureToggle = ButtonGuiElement.BASE_TEXTURE_TOGGLE;
        private ResourceLocation textureToggleHovered = ButtonGuiElement.BASE_TEXTURE_TOGGLE_HOVERED;
        private EditBox slots;

        public SplitButtonGuiElementBuilderPopup(BaseScreen parent, MutableProperties properties, @Nullable SplitButtonGuiElement from, Consumer<SplitButtonGuiElement> onFinish) {
            super(parent, properties, from, onFinish);
            if (from != null) {
                this.textureToggle = from.getTextureToggle();
                this.textureToggleHovered = from.getTextureToggleHovered();
            }
        }

        @Override
        public SplitButtonGuiElement makeElement() {
            List<String> slots = Arrays.stream(this.slots.getValue().split(",")).filter(s -> !s.isEmpty()).toList();
            return new SplitButtonGuiElement(this.properties.build(), this.textureToggle, this.textureToggleHovered, slots);
        }

        @Override
        public Component canCreate() {
            if (this.properties.getId().isEmpty())
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
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.split.slots"), this.font));
            this.slots = row.addChild(new EditBox(this.font, 100, 20, Component.empty()));
            this.slots.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.split.slots.tooltip")));
            if(this.baseElement != null)
                this.slots.setValue(this.listToString(this.baseElement.getSlots()));
        }

        private <T> String listToString(List<T> list) {
            StringBuilder builder = new StringBuilder();
            Iterator<T> iterator = list.iterator();
            while (iterator.hasNext()) {
                T next = iterator.next();
                builder.append(next.toString());
                if(iterator.hasNext())
                    builder.append(",");
            }
            return builder.toString();
        }
    }
}

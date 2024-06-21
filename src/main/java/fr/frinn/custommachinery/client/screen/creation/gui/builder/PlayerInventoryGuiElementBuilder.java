package fr.frinn.custommachinery.client.screen.creation.gui.builder;

import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiElementBuilderPopup;
import fr.frinn.custommachinery.client.screen.creation.gui.IGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.MutableProperties;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.common.guielement.PlayerInventoryGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElement.Properties;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class PlayerInventoryGuiElementBuilder implements IGuiElementBuilder<PlayerInventoryGuiElement> {

    @Override
    public GuiElementType<PlayerInventoryGuiElement> type() {
        return Registration.PLAYER_INVENTORY_GUI_ELEMENT.get();
    }

    @Override
    public PlayerInventoryGuiElement make(Properties properties, @Nullable PlayerInventoryGuiElement from) {
        return new PlayerInventoryGuiElement(properties);
    }

    @Override
    public PopupScreen makeConfigPopup(MachineEditScreen parent, MutableProperties properties, @Nullable PlayerInventoryGuiElement from, Consumer<PlayerInventoryGuiElement> onFinish) {
        return new PlayerInventoryGuiElementBuilderPopup(parent, properties, from, onFinish);
    }

    public static class PlayerInventoryGuiElementBuilderPopup extends GuiElementBuilderPopup<PlayerInventoryGuiElement> {

        public PlayerInventoryGuiElementBuilderPopup(BaseScreen parent, MutableProperties properties, @Nullable PlayerInventoryGuiElement from, Consumer<PlayerInventoryGuiElement> onFinish) {
            super(parent, properties, from, onFinish);
        }

        @Override
        public PlayerInventoryGuiElement makeElement() {
            return new PlayerInventoryGuiElement(this.properties.build());
        }

        @Override
        public void addWidgets(RowHelper row) {
            this.addTexture(row, Component.translatable("custommachinery.gui.creation.gui.texture"), this.properties::setTexture, this.baseElement != null ? this.baseElement.getTexture() : PlayerInventoryGuiElement.BASE_TEXTURE);
            this.addPriority(row);
        }
    }
}

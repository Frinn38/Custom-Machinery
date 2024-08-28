package fr.frinn.custommachinery.client.screen.creation.gui.builder;

import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiElementBuilderPopup;
import fr.frinn.custommachinery.client.screen.creation.gui.IGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.MutableProperties;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.common.guielement.FuelGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElement.Properties;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class FuelGuiElementBuilder implements IGuiElementBuilder<FuelGuiElement> {

    @Override
    public GuiElementType<FuelGuiElement> type() {
        return Registration.FUEL_GUI_ELEMENT.get();
    }

    @Override
    public FuelGuiElement make(Properties properties, @Nullable FuelGuiElement from) {
        if(from != null)
            return new FuelGuiElement(properties, from.getEmptyTexture(), from.getFilledTexture());
        else
            return new FuelGuiElement(properties, FuelGuiElement.BASE_EMPTY_TEXURE, FuelGuiElement.BASE_FILLED_TEXTURE);
    }

    @Override
    public PopupScreen makeConfigPopup(MachineEditScreen parent, MutableProperties properties, @Nullable FuelGuiElement from, Consumer<FuelGuiElement> onFinish) {
        return new FuelGuiElementBuilderPopup(parent, properties, from, onFinish);
    }

    public static class FuelGuiElementBuilderPopup extends GuiElementBuilderPopup<FuelGuiElement> {

        private ResourceLocation textureEmpty = FuelGuiElement.BASE_EMPTY_TEXURE;
        private ResourceLocation textureFilled = FuelGuiElement.BASE_FILLED_TEXTURE;

        public FuelGuiElementBuilderPopup(BaseScreen parent, MutableProperties properties, @Nullable FuelGuiElement from, Consumer<FuelGuiElement> onFinish) {
            super(parent, properties, from, onFinish);
            if(from != null) {
                this.textureEmpty = from.getEmptyTexture();
                this.textureFilled = from.getFilledTexture();
            }
        }

        @Override
        public FuelGuiElement makeElement() {
            return new FuelGuiElement(this.properties.build(), this.textureEmpty, this.textureFilled);
        }

        @Override
        public void addWidgets(RowHelper row) {
            this.addTexture(row, Component.translatable("custommachinery.gui.creation.gui.empty"), texture -> this.textureEmpty = texture, this.textureEmpty);
            this.addTexture(row, Component.translatable("custommachinery.gui.creation.gui.filled"), texture -> this.textureFilled = texture, this.textureFilled);
            this.addPriority(row);
        }
    }
}

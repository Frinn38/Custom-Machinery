package fr.frinn.custommachinery.client.screen.creation.gui.builder;

import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiElementBuilderPopup;
import fr.frinn.custommachinery.client.screen.creation.gui.IGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.MutableProperties;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.common.guielement.ExperienceGuiElement;
import fr.frinn.custommachinery.common.guielement.ExperienceGuiElement.DisplayMode;
import fr.frinn.custommachinery.common.guielement.ExperienceGuiElement.Mode;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElement.Properties;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ExperienceGuiElementBuilder implements IGuiElementBuilder<ExperienceGuiElement> {

    @Override
    public GuiElementType<ExperienceGuiElement> type() {
        return Registration.EXPERIENCE_GUI_ELEMENT.get();
    }

    @Override
    public ExperienceGuiElement make(Properties properties, @Nullable ExperienceGuiElement from) {
        if(from != null)
            return new ExperienceGuiElement(properties, from.getDisplayMode(), from.getMode());
        else
            return new ExperienceGuiElement(properties, DisplayMode.LEVEL, Mode.DISPLAY_BAR);
    }

    @Override
    public PopupScreen makeConfigPopup(MachineEditScreen parent, MutableProperties properties, @Nullable ExperienceGuiElement from, Consumer<ExperienceGuiElement> onFinish) {
        return new ExperienceGuiElementBuilderPopup(parent, properties, from, onFinish);
    }

    public static class ExperienceGuiElementBuilderPopup extends GuiElementBuilderPopup<ExperienceGuiElement> {

        private CycleButton<DisplayMode> displayMode;
        private CycleButton<Mode> mode;

        public ExperienceGuiElementBuilderPopup(BaseScreen parent, MutableProperties properties, @Nullable ExperienceGuiElement from, Consumer<ExperienceGuiElement> onFinish) {
            super(parent, properties, from, onFinish);
        }

        @Override
        public ExperienceGuiElement makeElement() {
            return new ExperienceGuiElement(this.properties.build(), this.displayMode.getValue(), this.mode.getValue());
        }

        @Override
        public void addWidgets(RowHelper row) {
            this.addTexture(row, Component.translatable("custommachinery.gui.creation.gui.texture"), this.properties::setTexture, this.baseElement != null ? this.baseElement.getTexture() : ExperienceGuiElement.BASE_TEXTURE);
            this.addTexture(row, Component.translatable("custommachinery.gui.creation.gui.texture_hovered"), this.properties::setTextureHovered, this.baseElement != null ? this.baseElement.getTextureHovered() : ExperienceGuiElement.BASE_TEXTURE_HOVERED);
            this.addPriority(row);
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.experience.display_mode"), this.font));
            this.displayMode = row.addChild(CycleButton.<DisplayMode>builder(display -> Component.literal(display.name())).withValues(DisplayMode.values()).withInitialValue(this.baseElement != null ? this.baseElement.getDisplayMode() : DisplayMode.LEVEL).displayOnlyValue().create(0, 0, 100, 20, Component.translatable("custommachinery.gui.creation.gui.experience.display_mode")));
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.experience.mode"), this.font));
            this.mode = row.addChild(CycleButton.<Mode>builder(mode -> Component.literal(mode.name())).withValues(Mode.values()).withInitialValue(this.baseElement != null ? this.baseElement.getMode() : Mode.DISPLAY_BAR).displayOnlyValue().create(0, 0, 100, 20, Component.translatable("custommachinery.gui.creation.gui.experience.mode")));
        }
    }
}

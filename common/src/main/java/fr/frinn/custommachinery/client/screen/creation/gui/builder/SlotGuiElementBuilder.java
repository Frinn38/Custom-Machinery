package fr.frinn.custommachinery.client.screen.creation.gui.builder;

import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.gui.IGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.common.guielement.SlotGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.GhostItem;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElement.Properties;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class SlotGuiElementBuilder implements IGuiElementBuilder<SlotGuiElement> {

    @Override
    public GuiElementType<SlotGuiElement> type() {
        return Registration.SLOT_GUI_ELEMENT.get();
    }

    @Override
    public SlotGuiElement make(Properties properties, @Nullable SlotGuiElement from) {
        if(from != null)
            return new SlotGuiElement(properties, from.getGhost());
        else
            return new SlotGuiElement(properties, GhostItem.EMPTY);
    }

    @Override
    public boolean hasExtraConfig() {
        return false;
    }

    @Override
    public PopupScreen makeConfigPopup(MachineEditScreen parent, Consumer<SlotGuiElement> onFinish) {
        return null;
    }
}

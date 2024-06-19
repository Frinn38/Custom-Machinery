package fr.frinn.custommachinery.client.screen.creation.gui;

import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElement.Properties;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;


public interface IGuiElementBuilder<T extends IGuiElement> {

    /**
     * @return A registered {@link GuiElementType} associated with the gui element to build.
     * This should be the same instance of {@link GuiElementType} as used when registering this builder.
     */
    GuiElementType<T> type();

    /**
     * @param properties Properties such as position and size are defined by the gui.
     * @param from If present, the returned element must copy extra properties from this element.
     * @return A default gui element of this type.
     */
    T make(Properties properties, @Nullable T from);

    /**
     * A popup that will be used to customize the gui element.
     * @param properties Basic properties of the element, position and size are defined by the gui.
     * @param from Will be present if this method is called to customize an existing element, or absent if this is a new element.
     * @param onFinish This callback must be called with the modified gui element once the popup is closed to save and apply the customization.
     * @return A {@link PopupScreen} to customize the gui element.
     */
    PopupScreen makeConfigPopup(MachineEditScreen parent, MutableProperties properties, @Nullable T from, Consumer<T> onFinish);
}

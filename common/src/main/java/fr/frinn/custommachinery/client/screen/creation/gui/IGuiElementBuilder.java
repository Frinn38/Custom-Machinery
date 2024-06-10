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
     * @param properties Properties such as position, size and textures are defined by the gui.
     * @param from If present, the returned element must copy extra properties from this element.
     * @return A default gui element of this type.
     */
    T make(Properties properties, @Nullable T from);

    /**
     * @return true if the gui element has extra customizable values, false otherwise.
     */
    boolean hasExtraConfig();

    /**
     * A popup that will be used to customize the gui element (outside the {@link Properties} values).
     * This will be called only if {@link IGuiElementBuilder#hasExtraConfig()} return true.
     * @param onFinish This callback must be called with the modified gui element once the popup is closed to save and apply the customization.
     * @return A {@link PopupScreen} to customize the gui element.
     */
    PopupScreen makeConfigPopup(MachineEditScreen parent, Consumer<T> onFinish);
}

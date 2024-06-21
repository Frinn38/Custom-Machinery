package fr.frinn.custommachinery.client.screen.creation.component;

import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface IMachineComponentBuilder<C extends IMachineComponent, T extends IMachineComponentTemplate<C>> {

    /**
     * @return A registered {@link MachineComponentType} associated with the component to build.
     * This should be the same instance of {@link MachineComponentType} as used when registering this builder.
     */
    MachineComponentType<C> type();

    /**
     * Create a popup to edit this component.
     * @param parent The screen that will manage this popup.
     * @param template The component template to edit, or null if that's a new component.
     * @param onFinish The popup must give the created/edited template to this consumer to make the machine save the changes.
     * @return A popup screen that will be used to configure all properties for a specific machine component.
     */
    PopupScreen makePopup(MachineEditScreen parent, @Nullable T template, Consumer<T> onFinish);

    /**
     * Can be used to render infos in the components list gui.
     * Do not render things outside the bounds, or it will get cropped.
     */
    void render(GuiGraphics graphics, int x, int y, int width, int height, T template);
}

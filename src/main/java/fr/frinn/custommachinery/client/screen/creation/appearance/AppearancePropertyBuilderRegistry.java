package fr.frinn.custommachinery.client.screen.creation.appearance;

import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import net.neoforged.fml.ModLoader;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class AppearancePropertyBuilderRegistry {

    private static Map<MachineAppearanceProperty<?>, IAppearancePropertyBuilder<?>> appearanceBuilders;

    public static void init() {
        RegisterAppearancePropertyBuilderEvent event = new RegisterAppearancePropertyBuilderEvent();
        ModLoader.postEventWrapContainerInModOrder(event);
        appearanceBuilders = event.getBuilders();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> IAppearancePropertyBuilder<T> getBuilder(MachineAppearanceProperty<T> property) {
        return (IAppearancePropertyBuilder<T>) appearanceBuilders.get(property);
    }
}

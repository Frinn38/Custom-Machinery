package fr.frinn.custommachinery.client.screen.creation.component;

import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class MachineComponentBuilderRegistry {

    private static Map<MachineComponentType<?>, IMachineComponentBuilder<?, ?>> componentBuilders;

    public static void init() {
        RegisterComponentBuilderEvent event = new RegisterComponentBuilderEvent();
        RegisterComponentBuilderEvent.EVENT.invoker().registerMachineComponentBuilders(event);
        componentBuilders = event.getBuilders();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <C extends IMachineComponent, T extends IMachineComponentTemplate<C>> IMachineComponentBuilder<C, T> getBuilder(MachineComponentType<C> type) {
        return (IMachineComponentBuilder<C, T>) componentBuilders.get(type);
    }
}

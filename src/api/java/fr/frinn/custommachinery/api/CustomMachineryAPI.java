package fr.frinn.custommachinery.api;

import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.utils.ICMLogger;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

public class CustomMachineryAPI {

    public static final String CM_MODID = "custommachinery";

    private static ICMLogger LOGGER;
    private static IForgeRegistry<MachineComponentType<?>> COMPONENT_REGISTRY;
    private static IForgeRegistry<GuiElementType<?>> GUI_ELEMENT_REGISTRY;

    public static ICMLogger getLogger() {
        if(LOGGER == null) {
            try {
                Class<?> loggerClass = Class.forName("fr.frinn.custommachinery.common.util.CMLogger");
                LOGGER = (ICMLogger) loggerClass.getField("INSTANCE").get(null);
            } catch (ReflectiveOperationException e) {
                System.out.println("Error while getting Custom Machinery Logger, is Custom Machinery present ?");
            }
        }
        return LOGGER;
    }

    /**
     * @return The forge registry for machine component types.
     * Do not use this for creating a DeferredRegister, it will probably return null at that time (before RegistryEvent.NewRegistry is run).
     * Instead, use DeferredRegister.create((Class)MachineComponentType.class, modid); (the cast is needed to stop generics for complaining).
     */
    public static IForgeRegistry<MachineComponentType<?>> getComponentRegistry() {
        if(COMPONENT_REGISTRY == null)
            COMPONENT_REGISTRY = RegistryManager.ACTIVE.getRegistry(MachineComponentType.class);
        return COMPONENT_REGISTRY;
    }

    /**
     * @return The forge registry for gui element types.
     * Do not use this for creating a DeferredRegister, it will probably return null at that time (before RegistryEvent.NewRegistry is run).
     * Instead, use DeferredRegister.create((Class)GuiElementType.class, modid); (the cast is needed to stop generics for complaining).
     */
    public static IForgeRegistry<GuiElementType<?>> getGuiElementRegistry() {
        if(GUI_ELEMENT_REGISTRY == null)
            GUI_ELEMENT_REGISTRY = RegistryManager.ACTIVE.getRegistry(GuiElementType.class);
        return GUI_ELEMENT_REGISTRY;
    }
}

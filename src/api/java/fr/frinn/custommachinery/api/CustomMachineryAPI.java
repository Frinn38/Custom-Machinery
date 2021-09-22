package fr.frinn.custommachinery.api;

import fr.frinn.custommachinery.api.components.MachineComponentType;
import fr.frinn.custommachinery.api.utils.ICMLogger;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

public class CustomMachineryAPI {

    public static final String CM_MODID = "custommachinery";

    private static ICMLogger LOGGER;
    private static IForgeRegistry<MachineComponentType<?>> COMPONENT_REGISTRY;

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
     * Do not use this for creating a DefferedRegister, it will probably return null at that time (before RegistryEvent.NewRegistry is run).
     * Instead, use DeferredRegister.create((Class)MachineComponentType.class, modid); (the cast is needed to stop generics for complaining.
     */
    public static IForgeRegistry<MachineComponentType<?>> getComponentRegistry() {
        if(COMPONENT_REGISTRY == null)
            COMPONENT_REGISTRY = RegistryManager.ACTIVE.getRegistry(MachineComponentType.class);
        return COMPONENT_REGISTRY;
    }
}

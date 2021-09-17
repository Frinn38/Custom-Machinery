package fr.frinn.custommachinery.api;

import fr.frinn.custommachinery.api.components.MachineComponentType;
import fr.frinn.custommachinery.api.network.DataType;
import fr.frinn.custommachinery.api.utils.ICMLogger;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

public class CustomMachineryAPI {

    public static final String CM_MODID = "custommachinery";

    private static ICMLogger LOGGER;
    private static IForgeRegistry<MachineComponentType<?>> COMPONENT_REGISTRY;
    private static IForgeRegistry<DataType<?, ?>> DATA_REGISTRY;

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

    public static IForgeRegistry<MachineComponentType<?>> getComponentRegistry() {
        if(COMPONENT_REGISTRY == null)
            COMPONENT_REGISTRY = RegistryManager.ACTIVE.getRegistry(MachineComponentType.class);
        return COMPONENT_REGISTRY;
    }

    public static IForgeRegistry<DataType<?, ?>> getDataRegistry() {
        if(DATA_REGISTRY == null)
            DATA_REGISTRY = RegistryManager.ACTIVE.getRegistry(DataType.class);
        return DATA_REGISTRY;
    }
}

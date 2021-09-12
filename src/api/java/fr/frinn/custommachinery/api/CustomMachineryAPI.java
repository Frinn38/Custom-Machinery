package fr.frinn.custommachinery.api;

import fr.frinn.custommachinery.api.components.IMachineComponent;
import fr.frinn.custommachinery.api.components.MachineComponentType;
import fr.frinn.custommachinery.api.network.DataType;
import fr.frinn.custommachinery.api.utils.CMLogger;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

public class CustomMachineryAPI {

    public static final String MODID = "custommachinery";
    public static final CMLogger LOGGER = new CMLogger();

    public static final ForgeRegistry<MachineComponentType<? extends IMachineComponent>> COMPONENT_TYPE_REGISTRY = RegistryManager.ACTIVE.getRegistry(new ResourceLocation(MODID, "component_type"));

    public static final ForgeRegistry<DataType<?, ?>> DATA_REGISTRY = RegistryManager.ACTIVE.getRegistry(new ResourceLocation(MODID, "data_type"));

    public static void info(String message, Object... args) {
        LOGGER.info(message, args);
    }

    public static void warn(String message, Object... args) {
        LOGGER.warn(message, args);
    }

    public static void error(String message, Object... args) {
        LOGGER.error(message, args);
    }
}

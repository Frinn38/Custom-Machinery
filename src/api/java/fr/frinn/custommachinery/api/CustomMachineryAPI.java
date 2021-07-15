package fr.frinn.custommachinery.api;

import fr.frinn.custommachinery.api.components.IMachineComponent;
import fr.frinn.custommachinery.api.components.MachineComponentType;
import fr.frinn.custommachinery.api.network.DataType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

public class CustomMachineryAPI {

    public static final String MODID = "custommachinery";

    public static ForgeRegistry<MachineComponentType<? extends IMachineComponent>> getComponentTypeRegistry() {
        return RegistryManager.ACTIVE.getRegistry(new ResourceLocation(MODID, "component_type"));
    }

    public static ForgeRegistry<DataType<?, ?>> getDataRegistry() {
        return RegistryManager.ACTIVE.getRegistry(new ResourceLocation(MODID, "data_type"));
    }


}

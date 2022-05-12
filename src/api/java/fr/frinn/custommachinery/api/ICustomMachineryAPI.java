package fr.frinn.custommachinery.api;

import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.api.network.DataType;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.api.utils.ICMLogger;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public interface ICustomMachineryAPI {

    ICustomMachineryAPI INSTANCE = Util.make(() -> {
        try {
            return (ICustomMachineryAPI)Class.forName("fr.frinn.custommachinery.CustomMachineryAPI").newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new IllegalStateException("Couldn't create Custom Machinery API instance", e);
        }
    });

    /**
     * The Custom Machinery Mod ID, this is currently "custommachinery" and should not change.
     */
    String modid();

    /**
     * Create a {@link ResourceLocation} for the specified path under the Custom Machinery namespace.
     * This is equivalent of doing <pre>new ResourceLocation("custommachinery", path);</pre>
     * @param path The path of the resource.
     * @return A {@link ResourceLocation} for the specified path under the Custom Machinery namespace.
     */
    ResourceLocation rl(String path);

    /**
     * Use this logger to write something to the custommachinery.log file.
     * @return The Custom Machinery logger.
     */
    ICMLogger logger();

    /**
     * @return The forge registry for machine component types.
     * Do not use this for creating a DeferredRegister, it will throw an exception if queried before the registry is created.
     * Instead, use DeferredRegister.create(MachineComponentType.REGISTRY_KEY, modid);
     */
    IForgeRegistry<MachineComponentType<?>> componentRegistry();

    /**
     * @return The forge registry for gui element types.
     * Do not use this for creating a DeferredRegister, it will throw an exception if queried before the registry is created.
     * Instead, use DeferredRegister.create(GuiElementType.REGISTRY_KEY, modid);
     */
    IForgeRegistry<GuiElementType<?>> guiElementRegistry();

    /**
     * @return The forge registry for requirement types.
     * Do not use this for creating a DeferredRegister, it will throw an exception if queried before the registry is created.
     * Instead, use DeferredRegister.create(RequirementType.REGISTRY_KEY, modid);
     */
    IForgeRegistry<RequirementType<?>> requirementRegistry();

    /**
     * @return The forge registry for appearance properties.
     * Do not use this for creating a DeferredRegister, it will throw an exception if queried before the registry is created.
     * Instead, use DeferredRegister.create(MachineAppearanceProperty.REGISTRY_KEY, modid);
     */
    IForgeRegistry<MachineAppearanceProperty<?>> appearancePropertyRegistry();

    /**
     * @return The forge registry for data types.
     * Do not use this for creating a DeferredRegister, it will throw an exception if queried before the registry is created.
     * Instead, use DeferredRegister.create(DataType.REGISTRY_KEY, modid);
     */
    IForgeRegistry<DataType<?, ?>> dataRegistry();
}

package fr.frinn.custommachinery.api;

import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.api.utils.ICMLogger;
import net.minecraft.util.Util;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

public class CustomMachineryAPI {

    /**
     * The Custom Machinery Mod ID, this is currently "custommachinery" and should not change.
     */
    public static final String MODID = Util.make(() -> {
        try {
            Class<?> custommachinery = Class.forName("fr.frinn.custommachinery.CustomMachinery");
            return (String)custommachinery.getField("MODID").get(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("CM API can't get the mod id from Custom Machinery, is Custom Machinery present ?");
        }
    });

    private static ICMLogger LOGGER;
    private static IForgeRegistry<MachineComponentType<?>> COMPONENT_REGISTRY;
    private static IForgeRegistry<GuiElementType<?>> GUI_ELEMENT_REGISTRY;
    private static IForgeRegistry<RequirementType<?>> REQUIREMENT_REGISTRY;

    /**
     * Use this logger to write something to the custommachinery.log file.
     * @return The Custom Machinery logger.
     */
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

    /**
     * @return The forge registry for requirement types.
     * Do not use this for creating a DeferredRegister, it will probably return null at that time (before RegistryEvent.NewRegistry is run).
     * Instead, use DeferredRegister.create((Class)RequirementType.class, modid); (the cast is needed to stop generics for complaining).
     */
    public static IForgeRegistry<RequirementType<?>> getRequirementRegistry() {
        if(REQUIREMENT_REGISTRY == null)
            REQUIREMENT_REGISTRY = RegistryManager.ACTIVE.getRegistry(RequirementType.class);
        return REQUIREMENT_REGISTRY;
    }
}

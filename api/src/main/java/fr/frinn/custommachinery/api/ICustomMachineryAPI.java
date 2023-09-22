package fr.frinn.custommachinery.api;

import dev.architectury.registry.registries.Registrar;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.component.variant.IComponentVariant;
import fr.frinn.custommachinery.api.crafting.ProcessorType;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.api.network.DataType;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.api.utils.ICMConfig;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;

public interface ICustomMachineryAPI {

    ICustomMachineryAPI INSTANCE = Util.make(() -> {
        try {
            return (ICustomMachineryAPI)Class.forName("fr.frinn.custommachinery.impl.CustomMachineryAPI").getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
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
    Logger logger();

    /**
     * Use this to access some CM config options.
     * @return The CM config.
     */
    ICMConfig config();

    /**
     * @return The registrar for machine component types.
     * Do not use this for creating a {@link dev.architectury.registry.registries.DeferredRegister}, it will throw an exception if queried before the registry is created.
     * Instead, use {@code DeferredRegister.create(modid, MachineComponentType.REGISTRY_KEY);}
     */
    Registrar<MachineComponentType<?>> componentRegistrar();

    /**
     * @return The registrar for gui element types.
     * Do not use this for creating a {@link dev.architectury.registry.registries.DeferredRegister}, it will throw an exception if queried before the registry is created.
     * Instead, use {@code DeferredRegister.create(modid, MachineComponentType.REGISTRY_KEY);}
     */
    Registrar<GuiElementType<?>> guiElementRegistrar();

    /**
     * @return The registrar for requirement types.
     * Do not use this for creating a {@link dev.architectury.registry.registries.DeferredRegister}, it will throw an exception if queried before the registry is created.
     * Instead, use {@code DeferredRegister.create(modid, MachineComponentType.REGISTRY_KEY);}
     */
    Registrar<RequirementType<?>> requirementRegistrar();

    /**
     * @return The registrar for appearance properties.
     * Do not use this for creating a {@link dev.architectury.registry.registries.DeferredRegister}, it will throw an exception if queried before the registry is created.
     * Instead, use {@code DeferredRegister.create(modid, MachineComponentType.REGISTRY_KEY);}
     */
    Registrar<MachineAppearanceProperty<?>> appearancePropertyRegistrar();

    /**
     * @return The registrar for data types.
     * Do not use this for creating a {@link dev.architectury.registry.registries.DeferredRegister}, it will throw an exception if queried before the registry is created.
     * Instead, use {@code DeferredRegister.create(modid, MachineComponentType.REGISTRY_KEY);}
     */
    Registrar<DataType<?, ?>> dataRegistrar();

    /**
     * @return The registrar for crafting processor types.
     * Do not use this for creating a {@link dev.architectury.registry.registries.DeferredRegister}, it will throw an exception if queried before the registry is created.
     * Instead, use {@code DeferredRegister.create(modid, CraftingProcessorType.REGISTRY_KEY);}
     */
    Registrar<ProcessorType<?>> processorRegistrar();

    /**
     * @return The registrar for the specified registry key.
     */
    <T> Registrar<T> registrar(ResourceKey<Registry<T>> registryKey);

    /**
     * @return A codec for the specified {@link MachineComponentType} and id, or null if this variant is not registered.
     */
    @Nullable
    <C extends IMachineComponent> NamedCodec<IComponentVariant> getVariantCodec(MachineComponentType<C> type, ResourceLocation id);
}

package fr.frinn.custommachinery.impl;

import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.Registrar;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.component.variant.IComponentVariant;
import fr.frinn.custommachinery.api.crafting.ProcessorType;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.api.network.DataType;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.api.utils.ICMConfig;
import fr.frinn.custommachinery.common.component.variant.ComponentVariantRegistry;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.CMLogger;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class CustomMachineryAPI implements ICustomMachineryAPI {

    @Override
    public String modid() {
        return CustomMachinery.MODID;
    }

    @Override
    public ResourceLocation rl(String path) {
        return ResourceLocation.tryParse(modid() + ":" + path);
    }

    @Override
    public Logger logger() {
        return CMLogger.INSTANCE;
    }

    @Override
    public ICMConfig config() {
        return CMConfigImpl.INSTANCE;
    }

    @Override
    public Registrar<MachineComponentType<?>> componentRegistrar() {
        return Registration.MACHINE_COMPONENT_TYPE_REGISTRY;
    }

    @Override
    public Registrar<GuiElementType<?>> guiElementRegistrar() {
        return Registration.GUI_ELEMENT_TYPE_REGISTRY;
    }

    @Override
    public Registrar<RequirementType<?>> requirementRegistrar() {
        return Registration.REQUIREMENT_TYPE_REGISTRY;
    }

    @Override
    public Registrar<MachineAppearanceProperty<?>> appearancePropertyRegistrar() {
        return Registration.APPEARANCE_PROPERTY_REGISTRY;
    }

    @Override
    public Registrar<DataType<?, ?>> dataRegistrar() {
        return Registration.DATA_REGISTRY;
    }

    @Override
    public Registrar<ProcessorType<?>> processorRegistrar() {
        return Registration.PROCESSOR_REGISTRY;
    }

    @Override
    public <T> Registrar<T> registrar(ResourceKey<Registry<T>> registryKey) {
        return Registration.REGISTRIES.get(registryKey);
    }

    @Nullable
    @Override
    public <C extends IMachineComponent> Codec<? extends IComponentVariant> getVariantCodec(MachineComponentType<C> type, ResourceLocation id) {
        return ComponentVariantRegistry.getVariantCodec(type, id);
    }
}

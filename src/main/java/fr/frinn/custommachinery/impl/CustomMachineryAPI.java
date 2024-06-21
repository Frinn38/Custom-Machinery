package fr.frinn.custommachinery.impl;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
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
import fr.frinn.custommachinery.common.component.variant.ComponentVariantRegistry;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.CMLogger;
import net.minecraft.core.Registry;
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
    public Registry<MachineComponentType<?>> componentRegistrar() {
        return Registration.MACHINE_COMPONENT_TYPE_REGISTRY;
    }

    @Override
    public Registry<GuiElementType<?>> guiElementRegistrar() {
        return Registration.GUI_ELEMENT_TYPE_REGISTRY;
    }

    @Override
    public Registry<RequirementType<?>> requirementRegistrar() {
        return Registration.REQUIREMENT_TYPE_REGISTRY;
    }

    @Override
    public Registry<MachineAppearanceProperty<?>> appearancePropertyRegistrar() {
        return Registration.APPEARANCE_PROPERTY_REGISTRY;
    }

    @Override
    public Registry<DataType<?, ?>> dataRegistrar() {
        return Registration.DATA_REGISTRY;
    }

    @Override
    public Registry<ProcessorType<?>> processorRegistrar() {
        return Registration.PROCESSOR_REGISTRY;
    }

    @Nullable
    @Override
    public <C extends IMachineComponent> NamedCodec<IComponentVariant> getVariantCodec(MachineComponentType<C> type, ResourceLocation id) {
        return ComponentVariantRegistry.getVariantCodec(type, id);
    }
}

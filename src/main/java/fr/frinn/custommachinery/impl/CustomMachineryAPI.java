package fr.frinn.custommachinery.impl;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.ProcessorType;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.api.network.DataType;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.common.util.CMLogger;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.Logger;

public class CustomMachineryAPI implements ICustomMachineryAPI {

    @Override
    public String modid() {
        return CustomMachinery.MODID;
    }

    @Override
    public Identifier rl(String path) {
        return Identifier.tryParse(modid() + ":" + path);
    }

    @Override
    public Logger logger() {
        return CMLogger.INSTANCE;
    }

    @Override
    public Registry<MachineComponentType<?>> componentRegistrar() {
        return CMRegistration.MACHINE_COMPONENT_TYPE_REGISTRY;
    }

    @Override
    public Registry<GuiElementType<?>> guiElementRegistrar() {
        return CMRegistration.GUI_ELEMENT_TYPE_REGISTRY;
    }

    @Override
    public Registry<RequirementType<?>> requirementRegistrar() {
        return CMRegistration.REQUIREMENT_TYPE_REGISTRY;
    }

    @Override
    public Registry<MachineAppearanceProperty<?>> appearancePropertyRegistrar() {
        return CMRegistration.APPEARANCE_PROPERTY_REGISTRY;
    }

    @Override
    public Registry<DataType<?, ?>> dataRegistrar() {
        return CMRegistration.DATA_REGISTRY;
    }

    @Override
    public Registry<ProcessorType<?>> processorRegistrar() {
        return CMRegistration.PROCESSOR_REGISTRY;
    }
}

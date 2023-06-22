package fr.frinn.custommachinery.common.machine;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.crafting.IProcessor;
import fr.frinn.custommachinery.api.crafting.IProcessorTemplate;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.machine.ICustomMachine;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.common.crafting.machine.MachineProcessor;
import fr.frinn.custommachinery.common.machine.builder.CustomMachineBuilder;
import fr.frinn.custommachinery.common.util.TextComponentUtils;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.List;

public class CustomMachine implements ICustomMachine {

    public static final NamedCodec<CustomMachine> CODEC = NamedCodec.record(machineCodec ->
        machineCodec.group(
                TextComponentUtils.CODEC.fieldOf("name").forGetter(machine -> machine.name),
                MachineAppearanceManager.CODEC.fieldOf("appearance").forGetter(machine -> machine.appearance),
                TextComponentUtils.CODEC.listOf().optionalFieldOf("tooltips", Collections.emptyList()).forGetter(CustomMachine::getTooltips),
                IGuiElement.CODEC.listOf().optionalFieldOf("gui", Collections.emptyList()).forGetter(CustomMachine::getGuiElements),
                IGuiElement.CODEC.listOf().optionalFieldOf("jei", Collections.emptyList()).forGetter(CustomMachine::getJeiElements),
                DefaultCodecs.RESOURCE_LOCATION.listOf().optionalFieldOf("catalysts", Collections.emptyList()).forGetter(CustomMachine::getCatalysts),
                IMachineComponentTemplate.CODEC.listOf().optionalFieldOf("components", Collections.emptyList()).forGetter(CustomMachine::getComponentTemplates),
                IProcessorTemplate.CODEC.optionalFieldOf("processor", MachineProcessor.Template.DEFAULT).forGetter(CustomMachine::getProcessorTemplate)
        ).apply(machineCodec, CustomMachine::new),
            "Custom machine"
    );

    public static final CustomMachine DUMMY = new CustomMachineBuilder()
            .setName(Component.literal("Dummy"))
            .setLocation(MachineLocation.fromDefault(new ResourceLocation(CustomMachinery.MODID, "dummy")))
            .build();

    private final Component name;
    private final MachineAppearanceManager appearance;
    private final List<Component> tooltips;
    private final List<IGuiElement> guiElements;
    private final List<IGuiElement> jeiElements;
    private final List<ResourceLocation> catalysts;
    private final List<IMachineComponentTemplate<? extends IMachineComponent>> componentTemplates;
    private final IProcessorTemplate<? extends IProcessor> processorTemplate;
    private MachineLocation location;


    public CustomMachine(Component name, MachineAppearanceManager appearance, List<Component> tooltips, List<IGuiElement> guiElements, List<IGuiElement> jeiElements, List<ResourceLocation> catalysts, List<IMachineComponentTemplate<? extends IMachineComponent>> componentTemplates, IProcessorTemplate<? extends IProcessor> processorTemplate) {
        this.name = name;
        this.appearance = appearance;
        this.tooltips = tooltips;
        this.guiElements = guiElements;
        this.jeiElements = jeiElements;
        this.catalysts = catalysts;
        this.componentTemplates = componentTemplates;
        this.processorTemplate = processorTemplate;
    }

    @Override
    public ResourceLocation getId() {
        return this.location.getId();
    }

    @Override
    public Component getName() {
        return this.name;
    }

    @Override
    public boolean isDummy() {
        return this == DUMMY;
    }

    @Override
    public MachineAppearance getAppearance(MachineStatus status) {
        return this.appearance.getAppearance(status);
    }

    @Override
    public IProcessorTemplate<? extends IProcessor> getProcessorTemplate() {
        return this.processorTemplate;
    }

    public List<Component> getTooltips() {
        return this.tooltips;
    }

    public List<IGuiElement> getGuiElements() {
        return this.guiElements;
    }

    public List<IGuiElement> getJeiElements() {
        return this.jeiElements;
    }

    public List<ResourceLocation> getCatalysts() {
        return this.catalysts;
    }

    public List<IMachineComponentTemplate<? extends IMachineComponent>> getComponentTemplates() {
        return this.componentTemplates;
    }

    public CustomMachine setLocation(MachineLocation location) {
        this.location = location;
        return this;
    }

    public MachineLocation getLocation() {
        return this.location;
    }
}

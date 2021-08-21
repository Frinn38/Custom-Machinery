package fr.frinn.custommachinery.common.data;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.components.IMachineComponent;
import fr.frinn.custommachinery.api.components.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.machine.ICustomMachine;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.common.data.builder.CustomMachineBuilder;
import fr.frinn.custommachinery.common.data.gui.IGuiElement;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class CustomMachine implements ICustomMachine {

    public static final Codec<CustomMachine> CODEC = RecordCodecBuilder.create(machineCodec ->
        machineCodec.group(
            Codec.STRING.fieldOf("name").forGetter(CustomMachine::getName),
            MachineAppearanceManager.CODEC.promotePartial(CustomMachinery.LOGGER::warn).fieldOf("appearance").forGetter(machine -> machine.appearance),
            IGuiElement.CODEC.listOf().optionalFieldOf("gui", ImmutableList.of()).forGetter(CustomMachine::getGuiElements),
            IGuiElement.CODEC.listOf().optionalFieldOf("jei", ImmutableList.of()).forGetter(CustomMachine::getJeiElements),
            IMachineComponentTemplate.CODEC.listOf().optionalFieldOf("components", new ArrayList<>()).forGetter(CustomMachine::getComponentTemplates)
        ).apply(machineCodec, CustomMachine::new)
    );

    public static final CustomMachine DUMMY = new CustomMachineBuilder()
            .setName("Dummy")
            .setLocation(MachineLocation.fromDefault(new ResourceLocation(CustomMachinery.MODID, "dummy")))
            .build();

    private String name;
    private MachineAppearanceManager appearance;
    private List<IGuiElement> guiElements;
    private List<IGuiElement> jeiElements;
    private List<IMachineComponentTemplate<? extends IMachineComponent>> componentTemplates;
    private MachineLocation location;


    public CustomMachine(String name, MachineAppearanceManager appearance, List<IGuiElement> guiElements, List<IGuiElement> jeiElements, List<IMachineComponentTemplate<? extends IMachineComponent>> componentTemplates) {
        this.name = name;
        this.appearance = appearance;
        this.guiElements = guiElements;
        this.jeiElements = jeiElements;
        this.componentTemplates = componentTemplates;
    }

    @Override
    public ResourceLocation getId() {
        return this.location.getId();
    }

    @Override
    public String getName() {
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

    public List<IGuiElement> getGuiElements() {
        return this.guiElements;
    }

    public List<IGuiElement> getJeiElements() {
        return this.jeiElements;
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

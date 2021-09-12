package fr.frinn.custommachinery.common.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.components.IMachineComponent;
import fr.frinn.custommachinery.api.components.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.machine.ICustomMachine;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.api.utils.CodecLogger;
import fr.frinn.custommachinery.common.data.builder.CustomMachineBuilder;
import fr.frinn.custommachinery.common.data.gui.IGuiElement;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.TextComponentUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.Collections;
import java.util.List;

public class CustomMachine implements ICustomMachine {

    public static final Codec<CustomMachine> CODEC = RecordCodecBuilder.create(machineCodec ->
        machineCodec.group(
                TextComponentUtils.CODEC.fieldOf("name").forGetter(machine -> machine.name),
                MachineAppearanceManager.CODEC.fieldOf("appearance").forGetter(machine -> machine.appearance),
                CodecLogger.loggedOptional(Codecs.list(IGuiElement.CODEC),"gui", Collections.emptyList()).forGetter(CustomMachine::getGuiElements),
                CodecLogger.loggedOptional(Codecs.list(IGuiElement.CODEC),"jei", Collections.emptyList()).forGetter(CustomMachine::getJeiElements),
                CodecLogger.loggedOptional(Codecs.list(IMachineComponentTemplate.CODEC),"components", Collections.emptyList()).forGetter(CustomMachine::getComponentTemplates)
        ).apply(machineCodec, CustomMachine::new)
    );

    public static final CustomMachine DUMMY = new CustomMachineBuilder()
            .setName(new StringTextComponent("Dummy"))
            .setLocation(MachineLocation.fromDefault(new ResourceLocation(CustomMachinery.MODID, "dummy")))
            .build();

    private ITextComponent name;
    private MachineAppearanceManager appearance;
    private List<IGuiElement> guiElements;
    private List<IGuiElement> jeiElements;
    private List<IMachineComponentTemplate<? extends IMachineComponent>> componentTemplates;
    private MachineLocation location;


    public CustomMachine(ITextComponent name, MachineAppearanceManager appearance, List<IGuiElement> guiElements, List<IGuiElement> jeiElements, List<IMachineComponentTemplate<? extends IMachineComponent>> componentTemplates) {
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
    public ITextComponent getName() {
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

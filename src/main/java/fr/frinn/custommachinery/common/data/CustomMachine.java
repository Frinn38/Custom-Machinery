package fr.frinn.custommachinery.common.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.CodecLogger;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.machine.ICustomMachine;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.common.data.builder.CustomMachineBuilder;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.TextComponentUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.List;

public class CustomMachine implements ICustomMachine {

    public static final Codec<CustomMachine> CODEC = RecordCodecBuilder.create(machineCodec ->
        machineCodec.group(
                TextComponentUtils.CODEC.fieldOf("name").forGetter(machine -> machine.name),
                MachineAppearanceManager.CODEC.fieldOf("appearance").forGetter(machine -> machine.appearance),
                CodecLogger.loggedOptional(Codecs.list(TextComponentUtils.CODEC), "tooltips", Collections.emptyList()).forGetter(CustomMachine::getTooltips),
                CodecLogger.loggedOptional(Codecs.list(IGuiElement.CODEC),"gui", Collections.emptyList()).forGetter(CustomMachine::getGuiElements),
                CodecLogger.loggedOptional(Codecs.list(IGuiElement.CODEC),"jei", Collections.emptyList()).forGetter(CustomMachine::getJeiElements),
                CodecLogger.loggedOptional(Codecs.list(ResourceLocation.CODEC), "catalysts", Collections.emptyList()).forGetter(CustomMachine::getCatalysts),
                CodecLogger.loggedOptional(Codecs.list(IMachineComponentTemplate.CODEC),"components", Collections.emptyList()).forGetter(CustomMachine::getComponentTemplates)
        ).apply(machineCodec, CustomMachine::new)
    );

    public static final CustomMachine DUMMY = new CustomMachineBuilder()
            .setName(new TextComponent("Dummy"))
            .setLocation(MachineLocation.fromDefault(new ResourceLocation(CustomMachinery.MODID, "dummy")))
            .build();

    private final Component name;
    private final MachineAppearanceManager appearance;
    private final List<Component> tooltips;
    private final List<IGuiElement> guiElements;
    private final List<IGuiElement> jeiElements;
    private final List<ResourceLocation> catalysts;
    private final List<IMachineComponentTemplate<? extends IMachineComponent>> componentTemplates;
    private MachineLocation location;


    public CustomMachine(Component name, MachineAppearanceManager appearance, List<Component> tooltips, List<IGuiElement> guiElements, List<IGuiElement> jeiElements, List<ResourceLocation> catalysts, List<IMachineComponentTemplate<? extends IMachineComponent>> componentTemplates) {
        this.name = name;
        this.appearance = appearance;
        this.tooltips = tooltips;
        this.guiElements = guiElements;
        this.jeiElements = jeiElements;
        this.catalysts = catalysts;
        this.componentTemplates = componentTemplates;
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

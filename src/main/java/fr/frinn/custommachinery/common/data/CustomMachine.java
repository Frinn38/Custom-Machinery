package fr.frinn.custommachinery.common.data;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.data.component.IMachineComponent;
import fr.frinn.custommachinery.common.data.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.common.data.gui.IGuiElement;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class CustomMachine {

    public static final Codec<CustomMachine> CODEC = RecordCodecBuilder.create(machineCodec ->
        machineCodec.group(
            Codec.STRING.fieldOf("name").forGetter(CustomMachine::getName),
            MachineAppearance.CODEC.fieldOf("appearance").forGetter(CustomMachine::getAppearance),
            IGuiElement.CODEC.listOf().fieldOf("gui").forGetter(CustomMachine::getGuiElements),
            IMachineComponentTemplate.CODEC.listOf().fieldOf("components").forGetter(CustomMachine::getComponentTemplates)
        ).apply(machineCodec, CustomMachine::new)
    );

    public static final CustomMachine DUMMY = new CustomMachine("Dummy", MachineAppearance.DUMMY, ImmutableList.of(), new ArrayList<>())
            .setId(new ResourceLocation(CustomMachinery.MODID + "dummy"));

    private ResourceLocation id;
    private String name;
    private MachineAppearance appearance;
    private List<IGuiElement> guiElements;
    private List<IMachineComponentTemplate<? extends IMachineComponent>> componentTemplates;

    public CustomMachine(String name, MachineAppearance appearance, List<IGuiElement> guiElements, List<IMachineComponentTemplate<? extends IMachineComponent>> componentTemplates) {
        this.name = name;
        this.appearance = appearance;
        this.guiElements = guiElements;
        this.componentTemplates = componentTemplates;
    }

    public CustomMachine setId(ResourceLocation id) {
        this.id = id;
        return this;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public MachineAppearance getAppearance() {
        return this.appearance;
    }

    public List<IGuiElement> getGuiElements() {
        return this.guiElements;
    }

    public List<IMachineComponentTemplate<? extends IMachineComponent>> getComponentTemplates() {
        return this.componentTemplates;
    }
}

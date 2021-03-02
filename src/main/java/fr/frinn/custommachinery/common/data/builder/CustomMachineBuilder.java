package fr.frinn.custommachinery.common.data.builder;

import com.google.common.collect.ImmutableList;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.data.MachineAppearance;
import fr.frinn.custommachinery.common.data.component.IMachineComponent;
import fr.frinn.custommachinery.common.data.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.common.data.gui.IGuiElement;
import fr.frinn.custommachinery.common.data.gui.MachineGUI;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class CustomMachineBuilder {

    private String name;
    private ResourceLocation id;
    private MachineAppearanceBuilder appearance;
    private List<IGuiElement> guiElements;
    private List<IMachineComponentTemplate<? extends IMachineComponent>> componentTemplates;

    public CustomMachineBuilder() {
        this.name = "New Machine";
        this.id = new ResourceLocation(CustomMachinery.MODID, "new_machine");
        this.appearance = new MachineAppearanceBuilder();
        this.guiElements = new ArrayList<>();
        this.componentTemplates = new ArrayList<>();
    }

    public CustomMachineBuilder(CustomMachine machine) {
        this.name = machine.getName();
        this.id = machine.getId();
        this.appearance = new MachineAppearanceBuilder(machine.getAppearance());
        this.guiElements = machine.getGuiElements();
        this.componentTemplates = machine.getComponentTemplates();
    }

    public String getName() {
        return this.name;
    }

    public CustomMachineBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public CustomMachineBuilder setId(ResourceLocation id) {
        this.id = id;
        return this;
    }

    public MachineAppearanceBuilder getAppearance() {
        return this.appearance;
    }

    public List<IGuiElement> getGuiElements() {
        return this.guiElements;
    }

    public List<IMachineComponentTemplate<? extends IMachineComponent>> getComponentTemplates() {
        return this.componentTemplates;
    }

    public CustomMachineBuilder setComponentTemplate(IMachineComponentTemplate<? extends IMachineComponent> template) {
        if(this.componentTemplates == null)
            this.componentTemplates = new ArrayList<>();
        this.componentTemplates.add(template);
        return this;
    }

    public CustomMachine build() {
        String name = this.name == null ? "New Machine" : this.name;
        ResourceLocation id = this.id == null ? new ResourceLocation(CustomMachinery.MODID, "new_machine") : this.id;
        MachineAppearance appearance = this.appearance.build();
        List<IGuiElement> guiElements = this.guiElements == null ? ImmutableList.of() : ImmutableList.copyOf(this.guiElements);
        List<IMachineComponentTemplate<? extends IMachineComponent>> componentTemplates = this.componentTemplates == null ? new ArrayList<>() : this.componentTemplates;
        return new CustomMachine(name, appearance, guiElements, componentTemplates).setId(id);
    }
}

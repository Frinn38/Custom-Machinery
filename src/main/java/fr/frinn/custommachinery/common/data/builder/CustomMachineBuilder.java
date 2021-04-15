package fr.frinn.custommachinery.common.data.builder;

import com.google.common.collect.ImmutableList;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.data.MachineAppearance;
import fr.frinn.custommachinery.common.data.MachineLocation;
import fr.frinn.custommachinery.common.data.component.IMachineComponent;
import fr.frinn.custommachinery.common.data.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.common.data.gui.IGuiElement;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class CustomMachineBuilder {

    private String name;
    private MachineAppearanceBuilder appearance;
    private List<IGuiElement> guiElements;
    private List<IMachineComponentTemplate<? extends IMachineComponent>> componentTemplates;
    private MachineLocation location;

    public CustomMachineBuilder() {
        this.name = "New Machine";
        this.appearance = new MachineAppearanceBuilder();
        this.guiElements = new ArrayList<>();
        this.componentTemplates = new ArrayList<>();
        this.location = MachineLocation.fromDefault(new ResourceLocation(CustomMachinery.MODID, "new_machine"));
    }

    public CustomMachineBuilder(CustomMachine machine) {
        this.name = machine.getName();
        this.appearance = new MachineAppearanceBuilder(machine.getAppearance());
        this.guiElements = machine.getGuiElements();
        this.componentTemplates = machine.getComponentTemplates();
        this.location = machine.getLocation();
    }

    public String getName() {
        return this.name;
    }

    public CustomMachineBuilder setName(String name) {
        this.name = name;
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

    public MachineLocation getLocation() {
        return this.location;
    }

    public CustomMachineBuilder setLocation(MachineLocation location) {
        this.location = location;
        return this;
    }

    public CustomMachineBuilder setId(ResourceLocation id) {
        MachineLocation.Loader loader = this.location.getLoader();
        String packName = this.location.getPackName();
        this.location = MachineLocation.fromDefault(id);
        return this;
    }

    public CustomMachine build() {
        String name = this.name == null ? "New Machine" : this.name;
        MachineAppearance appearance = this.appearance.build();
        List<IGuiElement> guiElements = this.guiElements == null ? ImmutableList.of() : ImmutableList.copyOf(this.guiElements);
        List<IMachineComponentTemplate<? extends IMachineComponent>> componentTemplates = this.componentTemplates == null ? new ArrayList<>() : this.componentTemplates;
        return new CustomMachine(name, appearance, guiElements, componentTemplates).setLocation(this.location);
    }
}

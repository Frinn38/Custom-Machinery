package fr.frinn.custommachinery.common.data.builder;

import com.google.common.collect.ImmutableList;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.data.MachineAppearance;
import fr.frinn.custommachinery.common.data.MachineLocation;
import fr.frinn.custommachinery.common.data.builder.component.IMachineComponentBuilder;
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
    private List<IMachineComponentBuilder<? extends IMachineComponent>> componentBuilders;
    private MachineLocation location;

    public CustomMachineBuilder() {
        this.name = "New Machine";
        this.appearance = new MachineAppearanceBuilder();
        this.guiElements = new ArrayList<>();
        this.componentBuilders = new ArrayList<>();
        this.location = MachineLocation.fromDefault(new ResourceLocation(CustomMachinery.MODID, "new_machine"));
    }

    public CustomMachineBuilder(CustomMachine machine) {
        this.name = machine.getName();
        this.appearance = new MachineAppearanceBuilder(machine.getAppearance());
        this.guiElements = machine.getGuiElements();
        this.componentBuilders = new ArrayList<>();
        machine.getComponentTemplates().forEach(template -> {
            if(template.getType().haveGUIBuilder())
                this.componentBuilders.add(template.getType().getGUIBuilder().get().fromComponent(template.build(null)));
        });
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

    public List<IMachineComponentBuilder<? extends IMachineComponent>> getComponentBuilders() {
        return this.componentBuilders;
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
        this.location = MachineLocation.fromLoader(loader, id, packName);
        return this;
    }

    public CustomMachine build() {
        String name = this.name == null ? "New Machine" : this.name;
        MachineAppearance appearance = this.appearance.build();
        List<IGuiElement> guiElements = this.guiElements == null ? ImmutableList.of() : ImmutableList.copyOf(this.guiElements);
        List<IMachineComponentTemplate<? extends IMachineComponent>> componentTemplates = new ArrayList<>();
        this.componentBuilders.forEach(builder -> componentTemplates.add(builder.build()));
        return new CustomMachine(name, appearance, guiElements, componentTemplates).setLocation(this.location);
    }
}

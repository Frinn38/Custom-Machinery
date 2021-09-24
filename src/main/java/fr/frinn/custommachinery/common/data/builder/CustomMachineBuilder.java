package fr.frinn.custommachinery.common.data.builder;

import com.google.common.collect.ImmutableList;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.component.builder.IMachineComponentBuilder;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.data.MachineAppearanceManager;
import fr.frinn.custommachinery.common.data.MachineLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CustomMachineBuilder {

    private ITextComponent name;
    private Map<MachineStatus, MachineAppearanceBuilder> appearance;
    private List<IGuiElement> guiElements;
    private List<IGuiElement> jeiElements;
    private List<IMachineComponentBuilder<? extends IMachineComponent>> componentBuilders;
    private MachineLocation location;

    public CustomMachineBuilder() {
        this.name = new StringTextComponent("New Machine");
        this.appearance = Arrays.stream(MachineStatus.values()).collect(Collectors.toMap(Function.identity(), status -> new MachineAppearanceBuilder()));
        this.guiElements = new ArrayList<>();
        this.jeiElements = new ArrayList<>();
        this.componentBuilders = new ArrayList<>();
        this.location = MachineLocation.fromDefault(new ResourceLocation(CustomMachinery.MODID, "new_machine"));
    }

    public CustomMachineBuilder(CustomMachine machine) {
        this.name = machine.getName();
        this.appearance = Arrays.stream(MachineStatus.values()).collect(Collectors.toMap(Function.identity(), status -> new MachineAppearanceBuilder(machine.getAppearance(status))));
        this.guiElements = machine.getGuiElements();
        this.jeiElements = machine.getJeiElements();
        this.componentBuilders = new ArrayList<>();
        machine.getComponentTemplates().forEach(template -> {
            if(template.getType().haveGUIBuilder())
                this.componentBuilders.add(template.getType().getGUIBuilder().get().fromComponent(template.build(null)));
        });
        this.location = machine.getLocation();
    }

    public ITextComponent getName() {
        return this.name;
    }

    public CustomMachineBuilder setName(ITextComponent name) {
        this.name = name;
        return this;
    }

    public CustomMachineBuilder withAppearance(MachineStatus status, MachineAppearanceBuilder builder) {
        this.appearance.put(status, builder);
        return this;
    }

    public MachineAppearanceBuilder getAppearance(MachineStatus status) {
        return this.appearance.get(status);
    }

    public CustomMachineBuilder withGuiElement(IGuiElement element) {
        this.guiElements.add(element);
        return this;
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
        ITextComponent name = this.name == null ? new StringTextComponent("New Machine") : this.name;
        MachineAppearanceManager appearance = new MachineAppearanceManager(this.appearance.get(MachineStatus.IDLE).build(), this.appearance.get(MachineStatus.RUNNING).build(), this.appearance.get(MachineStatus.ERRORED).build(), this.appearance.get(MachineStatus.PAUSED).build());
        List<IGuiElement> guiElements = this.guiElements == null ? ImmutableList.of() : ImmutableList.copyOf(this.guiElements);
        List<IGuiElement> jeiElements = this.jeiElements == null ? ImmutableList.of() : ImmutableList.copyOf(this.jeiElements);
        List<IMachineComponentTemplate<? extends IMachineComponent>> componentTemplates = new ArrayList<>();
        this.componentBuilders.forEach(builder -> componentTemplates.add(builder.build()));
        return new CustomMachine(name, appearance, guiElements, jeiElements, componentTemplates).setLocation(this.location);
    }
}

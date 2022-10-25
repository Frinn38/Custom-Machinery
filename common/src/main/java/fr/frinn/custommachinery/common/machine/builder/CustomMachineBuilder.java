package fr.frinn.custommachinery.common.machine.builder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.component.builder.IMachineComponentBuilder;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.common.crafting.machine.MachineProcessor;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.machine.MachineAppearanceManager;
import fr.frinn.custommachinery.common.machine.MachineLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CustomMachineBuilder {

    private Component name;
    private Map<MachineStatus, MachineAppearanceBuilder> appearance;
    private List<Component> tooltips;
    private List<IGuiElement> guiElements;
    private List<IGuiElement> jeiElements;
    private List<ResourceLocation> catalysts;
    private List<IMachineComponentBuilder<? extends IMachineComponent>> componentBuilders;
    private MachineLocation location;

    public CustomMachineBuilder() {
        this.name = new TextComponent("New Machine");
        this.appearance = Arrays.stream(MachineStatus.values()).collect(Collectors.toMap(Function.identity(), status -> new MachineAppearanceBuilder()));
        this.tooltips = new ArrayList<>();
        this.guiElements = new ArrayList<>();
        this.jeiElements = new ArrayList<>();
        this.catalysts = new ArrayList<>();
        this.componentBuilders = new ArrayList<>();
        this.location = MachineLocation.fromDefault(new ResourceLocation(CustomMachinery.MODID, "new_machine"));
    }

    public CustomMachineBuilder(CustomMachine machine) {
        this.name = machine.getName();
        this.appearance = Arrays.stream(MachineStatus.values()).collect(Collectors.toMap(Function.identity(), status -> new MachineAppearanceBuilder(machine.getAppearance(status))));
        this.tooltips = machine.getTooltips();
        this.guiElements = machine.getGuiElements();
        this.jeiElements = machine.getJeiElements();
        this.catalysts = machine.getCatalysts();
        this.componentBuilders = new ArrayList<>();
        machine.getComponentTemplates().forEach(template -> {
            if(template.getType().haveGUIBuilder())
                this.componentBuilders.add(template.getType().getGUIBuilder().get().fromComponent(template.build(null)));
        });
        this.location = machine.getLocation();
    }

    public Component getName() {
        return this.name;
    }

    public CustomMachineBuilder setName(Component name) {
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
        Component name = this.name == null ? new TextComponent("New Machine") : this.name;
        MachineAppearanceManager appearance = new MachineAppearanceManager(Maps.newHashMap(), this.appearance.get(MachineStatus.IDLE).build(), this.appearance.get(MachineStatus.RUNNING).build(), this.appearance.get(MachineStatus.ERRORED).build(), this.appearance.get(MachineStatus.PAUSED).build());
        List<Component> tooltips = this.tooltips == null ? ImmutableList.of() : ImmutableList.copyOf(this.tooltips);
        List<IGuiElement> guiElements = this.guiElements == null ? ImmutableList.of() : ImmutableList.copyOf(this.guiElements);
        List<IGuiElement> jeiElements = this.jeiElements == null ? ImmutableList.of() : ImmutableList.copyOf(this.jeiElements);
        List<ResourceLocation> catalysts = this.catalysts == null ? ImmutableList.of() : ImmutableList.copyOf(this.catalysts);
        List<IMachineComponentTemplate<? extends IMachineComponent>> componentTemplates = new ArrayList<>();
        this.componentBuilders.forEach(builder -> componentTemplates.add(builder.build()));
        return new CustomMachine(name, appearance, tooltips, guiElements, jeiElements, catalysts, componentTemplates, MachineProcessor.Template.DEFAULT).setLocation(this.location);
    }
}

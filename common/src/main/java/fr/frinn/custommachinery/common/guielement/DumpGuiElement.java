package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.codec.RegistrarCodec;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.List;

public class DumpGuiElement extends AbstractTexturedGuiElement {

    private static final ResourceLocation BASE_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_dump.png");

    public static final NamedCodec<DumpGuiElement> CODEC = NamedCodec.record(dumpGuiElement ->
            makeBaseTexturedCodec(dumpGuiElement, BASE_TEXTURE).and(dumpGuiElement.group(
                    RegistrarCodec.MACHINE_COMPONENT.listOf().optionalFieldOf("component", () -> Collections.singletonList(Registration.FLUID_MACHINE_COMPONENT.get())).forGetter(element -> element.components),
                    NamedCodec.STRING.listOf().fieldOf("id").forGetter(element -> element.id)
            )).apply(dumpGuiElement, DumpGuiElement::new), "Dump gui element"
    );

    private final List<MachineComponentType<?>> components;
    private final List<String> id;

    public DumpGuiElement(int x, int y, int width, int height, int priority, ResourceLocation texture, List<MachineComponentType<?>> components, List<String> id) {
        super(x, y, width, height, priority, texture);
        this.components = components;
        this.id = id;
    }

    public List<MachineComponentType<?>> getComponents() {
        return this.components;
    }

    @Override
    public GuiElementType<DumpGuiElement> getType() {
        return Registration.DUMP_GUI_ELEMENT.get();
    }

    @Override
    public void handleClick(byte button, MachineTile tile) {
        tile.getComponentManager()
                .getDumpComponents()
                .stream()
                .filter(component -> this.components.contains(component.getType()))
                .forEach(component -> component.dump(this.id));
    }
}

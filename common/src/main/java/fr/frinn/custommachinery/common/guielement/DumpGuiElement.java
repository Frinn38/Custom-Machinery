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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Collections;
import java.util.List;

public class DumpGuiElement extends AbstractTexturedGuiElement {

    private static final ResourceLocation BASE_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_dump.png");
    private static final ResourceLocation BASE_TEXTURE_HOVERED = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_dump_hovered.png");

    public static final NamedCodec<DumpGuiElement> CODEC = NamedCodec.record(dumpGuiElement ->
            dumpGuiElement.group(
                    makePropertiesCodec(BASE_TEXTURE, BASE_TEXTURE_HOVERED).forGetter(DumpGuiElement::getProperties),
                    RegistrarCodec.MACHINE_COMPONENT.listOf().optionalFieldOf("component", () -> Collections.singletonList(Registration.FLUID_MACHINE_COMPONENT.get())).forGetter(element -> element.components),
                    NamedCodec.STRING.listOf().fieldOf("tanks").forGetter(element -> element.tanks)
            ).apply(dumpGuiElement, DumpGuiElement::new), "Dump gui element"
    );

    private final List<MachineComponentType<?>> components;
    private final List<String> tanks;

    public DumpGuiElement(Properties properties, List<MachineComponentType<?>> components, List<String> tanks) {
        super(properties);
        this.components = components;
        this.tanks = tanks;
    }

    public List<MachineComponentType<?>> getComponents() {
        return this.components;
    }

    @Override
    public GuiElementType<DumpGuiElement> getType() {
        return Registration.DUMP_GUI_ELEMENT.get();
    }

    @Override
    public void handleClick(byte button, MachineTile tile, AbstractContainerMenu container, ServerPlayer player) {
        tile.getComponentManager()
                .getDumpComponents()
                .stream()
                .filter(component -> this.components.contains(component.getType()))
                .forEach(component -> component.dump(this.tanks));
    }
}

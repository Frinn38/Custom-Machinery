package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.PlatformHelper;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IComponentGuiElement;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class FluidGuiElement extends AbstractTexturedGuiElement implements IComponentGuiElement<FluidMachineComponent> {

    private static final ResourceLocation BASE_FLUID_STORAGE_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_fluid_storage.png");

    public static final NamedCodec<FluidGuiElement> CODEC = NamedCodec.record(fluidGuiElement ->
            makeBaseTexturedCodec(fluidGuiElement, BASE_FLUID_STORAGE_TEXTURE).and(
                    fluidGuiElement.group(
                            NamedCodec.STRING.fieldOf("id").forGetter(FluidGuiElement::getID),
                            NamedCodec.BOOL.optionalFieldOf("highlight", true).forGetter(FluidGuiElement::highlight)
                    )
            ).apply(fluidGuiElement, FluidGuiElement::new), "Fluid gui element"
    );

    private final String id;
    private final boolean highlight;

    public FluidGuiElement(int x, int y, int width, int height, int priority, ResourceLocation texture, String id, boolean highlight) {
        super(x, y, width, height, priority, texture);
        this.id = id;
        this.highlight = highlight;
    }

    public boolean highlight() {
        return this.highlight;
    }

    @Override
    public GuiElementType<FluidGuiElement> getType() {
        return Registration.FLUID_GUI_ELEMENT.get();
    }

    @Override
    public MachineComponentType<FluidMachineComponent> getComponentType() {
        return Registration.FLUID_MACHINE_COMPONENT.get();
    }

    @Override
    public String getID() {
        return this.id;
    }

    @Override
    public void handleClick(byte button, MachineTile tile, AbstractContainerMenu container, ServerPlayer player) {
        ItemStack carried = container.getCarried();
        if(carried.isEmpty() || !PlatformHelper.fluid().isFluidHandler(carried))
            return;

        tile.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(this.id))
                .ifPresent(component -> {
                    ItemStack stack = PlatformHelper.fluid().transferFluid(carried, component);
                    if(!player.isCreative())
                        container.setCarried(stack);
                });
    }
}

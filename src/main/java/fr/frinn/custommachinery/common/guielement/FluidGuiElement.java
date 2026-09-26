package fr.frinn.custommachinery.common.guielement;

import com.google.common.base.Predicates;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IComponentGuiElement;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement.Orientation;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import fr.frinn.custommachinery.impl.util.TextureInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public class FluidGuiElement extends AbstractTexturedGuiElement implements IComponentGuiElement<FluidMachineComponent> {

    public static final TextureInfo BASE_TEXTURE = CustomMachinery.texture("textures/gui/base_fluid_storage.png");

    public static final NamedCodec<FluidGuiElement> CODEC = NamedCodec.record(fluidGuiElement ->
            fluidGuiElement.group(
                    makePropertiesCodec(BASE_TEXTURE).forGetter(FluidGuiElement::getProperties),
                    NamedCodec.STRING.fieldOf("id").forGetter(FluidGuiElement::getId),
                    NamedCodec.BOOL.optionalFieldOf("highlight", true).forGetter(FluidGuiElement::highlight),
                    NamedCodec.enumCodec(Orientation.class).optionalFieldOf("orientation", Orientation.TOP).forGetter(FluidGuiElement::orientation)
            ).apply(fluidGuiElement, FluidGuiElement::new), "Fluid gui element"
    );

    private final boolean highlight;
    private final Orientation orientation;

    public FluidGuiElement(Properties properties, String id, boolean highlight, Orientation orientation) {
        super(properties);
        this.highlight = highlight;
        this.orientation = orientation;
    }

    public boolean highlight() {
        return this.highlight;
    }

    public Orientation orientation() {
        return this.orientation;
    }

    @Override
    public GuiElementType<FluidGuiElement> getType() {
        return CMRegistration.FLUID_GUI_ELEMENT.get();
    }

    @Override
    public MachineComponentType<FluidMachineComponent> getComponentType() {
        return CMRegistration.FLUID_MACHINE_COMPONENT.get();
    }

    @Override
    public String getComponentId() {
        return this.getId();
    }

    @Override
    public void handleClick(byte button, MachineTile tile, AbstractContainerMenu container, ServerPlayer player) {
        ItemStack carried = container.getCarried();

        if(carried.isEmpty())
            return;

        ResourceHandler<FluidResource> fluidHandlerItem = carried.getCapability(Capabilities.Fluid.ITEM, ItemAccess.forPlayerCursor(player, container));

        if(fluidHandlerItem == null)
            return;

        tile.getComponentManager().getComponentHandler(CMRegistration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(this.getId()))
                .ifPresent(component -> {
                    //Try empty item in component
                    if(component.getMode().isInput() && component.getCapacity() - component.getFluid().getAmount() > 0)
                        ResourceHandlerUtil.move(fluidHandlerItem, component, Predicates.alwaysTrue(), Integer.MAX_VALUE, null);
                    //Try empty component in item
                    else if(!component.getFluid().isEmpty())
                        ResourceHandlerUtil.move(component, fluidHandlerItem, Predicates.alwaysTrue(), Integer.MAX_VALUE, null);

                });
    }
}

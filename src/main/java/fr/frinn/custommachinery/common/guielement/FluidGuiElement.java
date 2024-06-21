package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IComponentGuiElement;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.forge.transfer.FluidTank;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;

public class FluidGuiElement extends AbstractTexturedGuiElement implements IComponentGuiElement<FluidMachineComponent> {

    public static final ResourceLocation BASE_TEXTURE = ResourceLocation.fromNamespaceAndPath(CustomMachinery.MODID, "textures/gui/base_fluid_storage.png");

    public static final NamedCodec<FluidGuiElement> CODEC = NamedCodec.record(fluidGuiElement ->
            fluidGuiElement.group(
                    makePropertiesCodec(BASE_TEXTURE).forGetter(FluidGuiElement::getProperties),
                    NamedCodec.STRING.fieldOf("id").forGetter(FluidGuiElement::getId),
                    NamedCodec.BOOL.optionalFieldOf("highlight", true).forGetter(FluidGuiElement::highlight)
            ).apply(fluidGuiElement, FluidGuiElement::new), "Fluid gui element"
    );

    private final boolean highlight;

    public FluidGuiElement(Properties properties, String id, boolean highlight) {
        super(properties);
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
    public String getComponentId() {
        return this.getId();
    }

    @Override
    public void handleClick(byte button, MachineTile tile, AbstractContainerMenu container, ServerPlayer player) {
        ItemStack carried = container.getCarried();
        IFluidHandlerItem handlerItem = carried.getCapability(FluidHandler.ITEM);
        if(carried.isEmpty() || handlerItem == null)
            return;

        tile.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(this.getId()))
                .ifPresent(component -> {
                    FluidActionResult result = FluidUtil.tryEmptyContainerAndStow(carried, new FluidTank(component), new PlayerMainInvWrapper(player.getInventory()), Integer.MAX_VALUE, player, true);
                    ItemStack stack = result.getResult();
                    if(result.isSuccess() && !player.isCreative()) {
                        if(carried.getCount() > 1) {
                            carried.shrink(1);
                            container.setCarried(carried);
                            player.addItem(stack);
                        } else
                            container.setCarried(stack);
                    }
                });
    }
}

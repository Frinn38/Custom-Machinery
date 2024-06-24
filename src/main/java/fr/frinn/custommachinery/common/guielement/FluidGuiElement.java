package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.CustomMachinery;
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
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.common.Tags.Items;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
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
        IFluidHandlerItem fluidHandlerItem = carried.getCapability(FluidHandler.ITEM);

        if(carried.isEmpty() || fluidHandlerItem == null)
            return;

        int testDrainAmount = carried.is(Items.BUCKETS) ? 1000 : 1;

        tile.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(handler -> handler.getComponentForID(this.getId()))
                .ifPresent(component -> {
                    FluidActionResult result = FluidActionResult.FAILURE;
                    //Try empty item in component
                    if(component.getMode().isInput() && component.getCapacity() - component.getFluid().getAmount() > 0 && !fluidHandlerItem.drain(testDrainAmount, FluidAction.SIMULATE).isEmpty())
                        result = FluidUtil.tryEmptyContainerAndStow(carried, component, new PlayerMainInvWrapper(player.getInventory()), Integer.MAX_VALUE, player, true);
                    //Try empty component in item
                    else if(!component.getFluid().isEmpty())
                        result = FluidUtil.tryFillContainerAndStow(carried, component, new PlayerMainInvWrapper(player.getInventory()), Integer.MAX_VALUE, player, true);

                    //In both case if success the carried item must be shrunk if player is not in creative mode
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

package fr.frinn.custommachinery.common.component.variant.item;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.handler.IComponentHandler;
import fr.frinn.custommachinery.api.component.variant.ITickableComponentVariant;
import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.component.variant.ItemComponentVariant;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FluidItemComponentVariant extends ItemComponentVariant implements ITickableComponentVariant<ItemMachineComponent> {

    public static final NamedCodec<FluidItemComponentVariant> CODEC = NamedCodec.record(variantInstance ->
            variantInstance.group(
                    NamedCodec.STRING.listOf().optionalFieldOf("tanks", Collections.emptyList()).forGetter(variant -> variant.tanks)
            ).apply(variantInstance, FluidItemComponentVariant::new), "Fluid item component variant"
    );
    public static final ResourceLocation ID = CustomMachinery.rl("fluid");

    private final List<String> tanks;

    public FluidItemComponentVariant(List<String> tanks) {
        this.tanks = tanks;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public NamedCodec<FluidItemComponentVariant> getCodec() {
        return CODEC;
    }

    @Override
    public boolean canAccept(IMachineComponentManager manager, ItemStack stack) {
        return stack.getCapability(FluidHandler.ITEM) != null;
    }

    @Override
    public void tick(ItemMachineComponent component) {
        ItemStack stack = component.getItemStack();
        if(stack.getCapability(FluidHandler.ITEM) == null)
            return;

        List<FluidMachineComponent> tanks = new ArrayList<>();
        if(this.tanks.isEmpty())
            tanks.addAll(component.getManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).map(IComponentHandler::getComponents).orElse(Collections.emptyList()));
        else {
            for(String tank : this.tanks) {
                component.getManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).flatMap(handler -> handler.getComponentForID(tank)).ifPresent(tanks::add);
            }
        }
        if(component.getMode().isInput()) {
            fillTanksFromStack(tanks, component);
        } else if(component.getMode().isOutput()) {
            fillStackFromTanks(component, tanks);
        }
    }

    public static void fillTanksFromStack(List<FluidMachineComponent> tanks, ItemMachineComponent slot) {
        ItemStack stack = slot.getItemStack();
        if(stack.isEmpty())
            return;

        IFluidHandlerItem handlerItem =  stack.getCapability(FluidHandler.ITEM);
        if(handlerItem == null)
            return;

        for(FluidMachineComponent component : tanks) {
            FluidStack maxExtract;
            if(component.getFluid().isEmpty())
                maxExtract = handlerItem.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
            else
                maxExtract = handlerItem.drain(new FluidStack(component.getFluid().getFluid(), Integer.MAX_VALUE), FluidAction.SIMULATE);

            if(maxExtract.isEmpty())
                continue;

            long maxInsert = component.fillBypassLimit(maxExtract, FluidAction.SIMULATE);

            if(maxInsert <= 0)
                continue;

            FluidStack extracted = handlerItem.drain(new FluidStack(maxExtract.getFluid(), Utils.toInt(maxInsert)), FluidAction.EXECUTE);

            if(extracted.getAmount() > 0)
                component.fillBypassLimit(extracted, FluidAction.EXECUTE);
        }
        slot.setItemStack(handlerItem.getContainer());
    }

    public static void fillStackFromTanks(ItemMachineComponent slot, List<FluidMachineComponent> tanks) {
        ItemStack stack = slot.getItemStack();
        if(stack.isEmpty())
            return;

        IFluidHandlerItem handlerItem = stack.getCapability(FluidHandler.ITEM);
        if(handlerItem == null)
            return;

        for(FluidMachineComponent component : tanks) {
            for(int i = 0; i < handlerItem.getTanks(); i++) {
                if(handlerItem.getFluidInTank(i).isEmpty() || FluidStack.isSameFluidSameComponents(handlerItem.getFluidInTank(i), component.getFluid())) {
                    FluidStack maxExtract = component.drainBypassLimit(Integer.MAX_VALUE, FluidAction.SIMULATE);

                    if(maxExtract.isEmpty())
                        continue;

                    int maxInsert = handlerItem.fill(maxExtract, FluidAction.SIMULATE);

                    if(maxInsert <= 0)
                        continue;

                    FluidStack extracted = component.drainBypassLimit(maxInsert, FluidAction.EXECUTE);

                    if(extracted.getAmount() > 0)
                        handlerItem.fill(extracted, FluidAction.EXECUTE);
                }
            }
        }
        slot.setItemStack(handlerItem.getContainer());
    }
}

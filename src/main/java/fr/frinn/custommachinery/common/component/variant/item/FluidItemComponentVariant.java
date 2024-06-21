package fr.frinn.custommachinery.common.component.variant.item;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.handler.IComponentHandler;
import fr.frinn.custommachinery.api.component.variant.ITickableComponentVariant;
import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.forge.transfer.ForgeFluidHelper;
import fr.frinn.custommachinery.impl.component.variant.ItemComponentVariant;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

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
        return ForgeFluidHelper.isFluidHandler(stack);
    }

    @Override
    public void tick(ItemMachineComponent component) {
        ItemStack stack = component.getItemStack();
        if(!ForgeFluidHelper.isFluidHandler(stack))
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
            ForgeFluidHelper.fillTanksFromStack(tanks, component);
        } else if(component.getMode().isOutput()) {
            ForgeFluidHelper.fillStackFromTanks(component, tanks);
        }
    }
}

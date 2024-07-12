package fr.frinn.custommachinery.common.component.item;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.ITickableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.utils.Filter;
import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FluidHandlerItemMachineComponent extends ItemMachineComponent implements ITickableComponent {

    private final List<String> tanks;

    public FluidHandlerItemMachineComponent(IMachineComponentManager manager, ComponentIOMode mode, String id, int capacity, int maxInput, int maxOutput, Filter<Item> filter, SideConfig.Template configTemplate, boolean locked, List<String> tanks) {
        super(manager, mode, id, capacity, maxInput, maxOutput, filter, configTemplate, locked);
        this.tanks = tanks;
    }

    @Override
    public MachineComponentType<ItemMachineComponent> getType() {
        return Registration.ITEM_FLUID_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return super.isItemValid(slot, stack) && stack.getCapability(FluidHandler.ITEM) != null;
    }

    @Override
    public void serverTick() {
        ItemStack stack = this.getItemStack();
        if(stack.getCapability(FluidHandler.ITEM) == null)
            return;

        List<FluidMachineComponent> tanks = new ArrayList<>();
        if(this.tanks.isEmpty())
            tanks.addAll(this.getManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).map(handler -> handler.getComponents().stream().filter(component -> component.getMode().isInput() || component.getMode().isOutput() == this.getMode().isOutput()).toList()).orElse(Collections.emptyList()));
        else {
            for(String tank : this.tanks) {
                this.getManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).flatMap(handler -> handler.getComponentForID(tank)).ifPresent(tanks::add);
            }
        }
        if(this.getMode().isInput()) {
            fillTanksFromStack(tanks, this);
        } else if(this.getMode().isOutput()) {
            fillStackFromTanks(this, tanks);
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

            int maxInsert = component.fillBypassLimit(maxExtract, FluidAction.SIMULATE);

            if(maxInsert <= 0)
                continue;

            FluidStack extracted = handlerItem.drain(new FluidStack(maxExtract.getFluid(), maxInsert), FluidAction.EXECUTE);

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

    public static class Template extends ItemMachineComponent.Template {

        public static final NamedCodec<Template> CODEC = NamedCodec.record(instance ->
                instance.group(
                        NamedCodec.STRING.fieldOf("id").forGetter(template -> template.id),
                        ComponentIOMode.CODEC.optionalFieldOf("mode", ComponentIOMode.BOTH).forGetter(template -> template.mode),
                        NamedCodec.INT.optionalFieldOf("capacity", 64).forGetter(template -> template.capacity),
                        NamedCodec.INT.optionalFieldOf("max_input").forGetter(template -> template.maxInput == template.capacity ? Optional.empty() : Optional.of(template.maxInput)),
                        NamedCodec.INT.optionalFieldOf("max_output").forGetter(template -> template.maxOutput == template.capacity ? Optional.empty() : Optional.of(template.maxOutput)),
                        Filter.codec(DefaultCodecs.registryValueOrTag(BuiltInRegistries.ITEM)).forGetter(template -> template.filter),
                        SideConfig.Template.CODEC.optionalFieldOf("config").forGetter(template -> template.config == template.mode.getBaseConfig() ? Optional.empty() : Optional.of(template.config)),
                        NamedCodec.BOOL.optionalFieldOf("locked", false).aliases("lock").forGetter(template -> template.locked),
                        NamedCodec.STRING.listOf().optionalFieldOf("tanks", Collections.emptyList()).forGetter(template -> template.tanks)
                ).apply(instance, Template::new), "Fluid handler item machine component");

        public final List<String> tanks;

        public Template(String id, ComponentIOMode mode, int capacity, Optional<Integer> maxInput, Optional<Integer> maxOutput, Filter<Item> filter, Optional<SideConfig.Template> config, boolean locked, List<String> tanks) {
            super(id, mode, capacity, maxInput, maxOutput, filter, config, locked);
            this.tanks = tanks;
        }

        @Override
        public boolean isItemValid(IMachineComponentManager manager, ItemStack stack) {
            return stack.getCapability(FluidHandler.ITEM) != null;
        }

        @Override
        public MachineComponentType<ItemMachineComponent> getType() {
            return Registration.ITEM_FLUID_MACHINE_COMPONENT.get();
        }

        @Override
        public FluidHandlerItemMachineComponent build(IMachineComponentManager manager) {
            return new FluidHandlerItemMachineComponent(manager, this.mode, this.id, this.capacity, this.maxInput, this.maxOutput, this.filter, this.config, this.locked, this.tanks);
        }
    }
}

package fr.frinn.custommachinery.common.component.item;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.ITickableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.common.util.Filter;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.component.config.IOSideConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FluidHandlerItemMachineComponent extends ItemMachineComponent implements ITickableComponent {

    private final List<String> tanks;

    public FluidHandlerItemMachineComponent(IMachineComponentManager manager, ComponentIOMode mode, String id, int capacity, int maxInput, int maxOutput, Filter<Item> filter, IOSideConfig.Template configTemplate, boolean locked, List<String> tanks) {
        super(manager, mode, id, capacity, maxInput, maxOutput, filter, configTemplate, locked);
        this.tanks = tanks;
    }

    @Override
    public MachineComponentType<ItemMachineComponent> getType() {
        return CMRegistration.ITEM_FLUID_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return super.isValid(index, resource) && resource.toStack().getCapability(Capabilities.Fluid.ITEM, ItemAccess.forStack(resource.toStack())) != null;
    }

    @Override
    public void serverTick() {
        ItemStack stack = this.getItemStack();
        if(stack.getCapability(Capabilities.Fluid.ITEM, ItemAccess.forHandlerIndexStrict(this, 0)) == null)
            return;

        List<FluidMachineComponent> tanks = new ArrayList<>();
        if(this.tanks.isEmpty())
            tanks.addAll(this.getManager().getComponentHandler(CMRegistration.FLUID_MACHINE_COMPONENT.get()).map(handler -> handler.getComponents().stream().filter(component -> component.getMode().isInput() || component.getMode().isOutput() == this.getMode().isOutput()).toList()).orElse(Collections.emptyList()));
        else {
            for(String tank : this.tanks) {
                this.getManager().getComponentHandler(CMRegistration.FLUID_MACHINE_COMPONENT.get()).flatMap(handler -> handler.getComponentForID(tank)).ifPresent(tanks::add);
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

        ResourceHandler<FluidResource> fluidHandler =  stack.getCapability(Capabilities.Fluid.ITEM, ItemAccess.forHandlerIndexStrict(slot, 0));
        if(fluidHandler == null)
            return;

        FluidStack fluidStack = FluidUtil.getFirstStackContained(stack);
        if(fluidStack.isEmpty())
            return;

        FluidResource resource = FluidResource.of(fluidStack);

        try(Transaction transaction = Transaction.openRoot()) {
            for(FluidMachineComponent component : tanks) {
                int extracted = fluidHandler.extract(resource, Integer.MAX_VALUE, transaction);

                if(extracted == 0)
                    continue;

                component.insertBypassLimit(resource, extracted, transaction);
            }
            transaction.commit();
        }
    }

    public static void fillStackFromTanks(ItemMachineComponent slot, List<FluidMachineComponent> tanks) {
        ItemStack stack = slot.getItemStack();
        if(stack.isEmpty())
            return;

        ResourceHandler<FluidResource> fluidHandler = stack.getCapability(Capabilities.Fluid.ITEM, ItemAccess.forHandlerIndexStrict(slot, 0));
        if(fluidHandler == null)
            return;

        try(Transaction transaction = Transaction.openRoot()) {
            for(FluidMachineComponent component : tanks) {
                for(int i = 0; i < fluidHandler.size(); i++) {
                    if(fluidHandler.getResource(i).isEmpty() || fluidHandler.getResource(i).matches(component.getFluid())) {
                        FluidResource resource = component.getResource(0);

                        if(resource.isEmpty())
                            continue;

                        int maxExtract = component.extractBypassLimit(resource, Integer.MAX_VALUE, transaction);

                        if(maxExtract == 0)
                            continue;

                        fluidHandler.insert(resource, maxExtract, transaction);
                    }
                }
            }
            transaction.commit();
        }
    }

    public static class Template extends ItemMachineComponent.Template {

        public static final NamedCodec<Template> CODEC = NamedCodec.record(instance ->
                instance.group(
                        NamedCodec.STRING.fieldOf("id").forGetter(template -> template.id),
                        ComponentIOMode.CODEC.optionalFieldOf("mode", ComponentIOMode.BOTH).forGetter(template -> template.mode),
                        NamedCodec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("capacity", Integer.MAX_VALUE).forGetter(template -> template.capacity),
                        NamedCodec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("max_input").forGetter(template -> template.maxInput == template.capacity ? Optional.empty() : Optional.of(template.maxInput)),
                        NamedCodec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("max_output").forGetter(template -> template.maxOutput == template.capacity ? Optional.empty() : Optional.of(template.maxOutput)),
                        Filter.codec(DefaultCodecs.registryValueOrTag(BuiltInRegistries.ITEM)).orElse(Filter.empty()).forGetter(template -> template.filter),
                        IOSideConfig.Template.CODEC.optionalFieldOf("config").forGetter(template -> template.config == template.mode.getBaseConfig() ? Optional.empty() : Optional.of(template.config)),
                        NamedCodec.BOOL.optionalFieldOf("locked", false).aliases("lock").forGetter(template -> template.locked),
                        NamedCodec.STRING.listOf().optionalFieldOf("tanks", Collections.emptyList()).forGetter(template -> template.tanks)
                ).apply(instance, (id, mode, capacity, maxInput, maxOutput, filter, config, locked, tanks) ->
                        new Template(id, mode, capacity, maxInput.orElse(capacity), maxOutput.orElse(capacity), filter, config.orElse(mode.getBaseConfig()), locked, tanks)), "Fluid handler item machine component");

        public final List<String> tanks;

        public Template(String id, ComponentIOMode mode, int capacity, int maxInput, int maxOutput, Filter<Item> filter, IOSideConfig.Template config, boolean locked, List<String> tanks) {
            super(id, mode, capacity, maxInput, maxOutput, filter, config, locked);
            this.tanks = tanks;
        }

        @Override
        public boolean isItemValid(IMachineComponentManager manager, ItemStack stack) {
            return stack.getCapability(Capabilities.Fluid.ITEM, ItemAccess.forStack(stack)) != null;
        }

        @Override
        public MachineComponentType<ItemMachineComponent> getType() {
            return CMRegistration.ITEM_FLUID_MACHINE_COMPONENT.get();
        }

        @Override
        public FluidHandlerItemMachineComponent build(IMachineComponentManager manager) {
            return new FluidHandlerItemMachineComponent(manager, this.mode, this.id, this.capacity, this.maxInput, this.maxOutput, this.filter, this.config, this.locked, this.tanks);
        }
    }
}

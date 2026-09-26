package fr.frinn.custommachinery.common.component.handler;

import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import fr.frinn.custommachinery.api.component.IDumpComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.ISerializableComponent;
import fr.frinn.custommachinery.api.component.ITickableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.common.util.transfer.InteractionFluidHandler;
import fr.frinn.custommachinery.common.util.transfer.SidedFluidHandler;
import fr.frinn.custommachinery.impl.component.AbstractComponentHandler;
import fr.frinn.custommachinery.impl.component.config.IOSideMode;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.ValueOutputList;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FluidComponentHandler extends AbstractComponentHandler<FluidMachineComponent> implements ISerializableComponent, ISyncableStuff, ITickableComponent, IDumpComponent {

    public final InteractionFluidHandler interactionFluidHandler = new InteractionFluidHandler(this);
    private final SidedFluidHandler generalHandler = new SidedFluidHandler(null, this);
    private final Map<Direction, SidedFluidHandler> sidedHandlers = Maps.newEnumMap(Direction.class);
    private final Map<Direction, BlockCapabilityCache<ResourceHandler<FluidResource>, Direction>> neighbourStorages = Maps.newEnumMap(Direction.class);

    public FluidComponentHandler(IMachineComponentManager manager, List<FluidMachineComponent> components) {
        super(manager, components);
        components.forEach(component -> {
            component.getConfig().setCallback(this::configChanged);
            if(component.getMode().isInput())
                this.inputs.add(component);
            if(component.getMode().isOutput())
                this.outputs.add(component);
        });
        for(Direction side : Direction.values())
            this.sidedHandlers.put(side, new SidedFluidHandler(side, this));
    }

    public void configChanged(RelativeSide side, IOSideMode oldMode, IOSideMode newMode) {
        if(oldMode.isNone() != newMode.isNone())
            this.getManager().getTile().invalidateCapabilities();
    }

    @Nullable
    public ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        if(side == null)
            return this.generalHandler;
        else if(this.getComponents().stream().anyMatch(component -> !component.getConfig().getDirectionMode(side).isNone()))
            return this.sidedHandlers.get(side);
        return null;
    }

    @Override
    public MachineComponentType<FluidMachineComponent> getType() {
        return CMRegistration.FLUID_MACHINE_COMPONENT.get();
    }

    @Override
    public Optional<FluidMachineComponent> getComponentForID(String id) {
        return this.getComponents().stream().filter(component -> component.getId().equals(id)).findFirst();
    }

    @Override
    public void serverTick() {
        //I/O between the machine and neighbor blocks.
        for(Direction side : Direction.values()) {
            if(this.getComponents().stream().noneMatch(component -> component.getConfig().canAutoIO(side)))
                continue;

            if(this.neighbourStorages.get(side) == null)
                this.neighbourStorages.put(side, BlockCapabilityCache.create(Capabilities.Fluid.BLOCK, (ServerLevel)this.getManager().getLevel(), this.getManager().getTile().getBlockPos().relative(side), side.getOpposite(), () -> !this.getManager().getTile().isRemoved(), () -> this.neighbourStorages.remove(side)));

            ResourceHandler<FluidResource> neighbour = this.neighbourStorages.get(side).getCapability();

            if(neighbour == null)
                continue;

            this.getComponents().forEach(component -> {
                if(component.getConfig().isAutoInput() && component.getConfig().getDirectionMode(side).isInput() && component.getFluid().getAmount() < component.getCapacity())
                    ResourceHandlerUtil.move(neighbour, component, Predicates.alwaysTrue(), Integer.MAX_VALUE, null);

                if(component.getConfig().isAutoOutput() && component.getConfig().getDirectionMode(side).isOutput() && component.getFluid().getAmount() > 0)
                    ResourceHandlerUtil.move(component, neighbour, Predicates.alwaysTrue(), Integer.MAX_VALUE, null);
            });
        }
    }

    @Override
    public void serialize(ValueOutput output) {
        ValueOutputList list = output.childrenList("fluids");
        this.getComponents().forEach(component -> {
            ValueOutput child = list.addChild();
            component.serialize(child);
            child.putString("id", component.getId());
        });
    }

    @Override
    public void deserialize(ValueInput input) {
        input.childrenList("fluids").ifPresent(list -> list.forEach(child -> child.getString("id").flatMap(this::getComponentForID).ifPresent(component -> component.deserialize(child))));
    }

    @Override
    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        this.getComponents().forEach(component -> component.getStuffToSync(container));
    }

    @Override
    public void dump(List<String> ids) {
        this.getComponents().stream()
                .filter(component -> ids.contains(component.getId()))
                .forEach(component -> component.setFluidStack(FluidStack.EMPTY));
    }

    /** RECIPE STUFF **/

    private final List<FluidMachineComponent> inputs = new ArrayList<>();
    private final List<FluidMachineComponent> outputs = new ArrayList<>();

    public int getIngredientAmount(String tank, FluidIngredient ingredient) {
        Predicate<FluidMachineComponent> tankPredicate = component -> tank.isEmpty() || component.getId().equals(tank);
        return this.inputs.stream()
                .filter(component -> ingredient.test(component.getFluid()) && tankPredicate.test(component))
                .mapToInt(component -> component.getFluid().getAmount())
                .sum();
    }

    public int getSpaceForFluid(String tank, FluidStack stack) {
        Predicate<FluidMachineComponent> tankPredicate = component -> tank.isEmpty() || component.getId().equals(tank);
        return this.outputs.stream()
                .filter(component -> component.isValid(0, FluidResource.of(stack)) && tankPredicate.test(component))
                .mapToInt(FluidMachineComponent::getRecipeRemainingSpace)
                .sum();
    }

    public void removeFromInputs(String tank, FluidIngredient ingredient, int amount) {
        AtomicInteger toRemove = new AtomicInteger(amount);
        Predicate<FluidMachineComponent> tankPredicate = component -> tank.isEmpty() || component.getId().equals(tank);
        this.inputs.stream().filter(component -> ingredient.test(component.getFluid()) && tankPredicate.test(component)).forEach(component -> {
            int maxExtract = Math.min(component.getFluid().getAmount(), toRemove.get());
            toRemove.addAndGet(-maxExtract);
            component.recipeExtract(maxExtract);
        });
    }

    public void addToOutputs(String tank, FluidStack stack) {
        AtomicInteger toAdd = new AtomicInteger(stack.getAmount());
        Predicate<FluidMachineComponent> tankPredicate = component -> tank.isEmpty() || component.getId().equals(tank);
        this.outputs.stream()
                .filter(component -> component.isValid(0, FluidResource.of(stack)) && tankPredicate.test(component))
                .sorted(Comparator.comparingInt(component -> FluidStack.isSameFluidSameComponents(component.getFluid(), stack) ? -1 : 1))
                .forEach(component -> {
                    int maxInsert = Math.min(component.getRecipeRemainingSpace(), toAdd.get());
                    toAdd.addAndGet(-maxInsert);
                    component.recipeInsert(stack.copy(), maxInsert);
                });
    }
}

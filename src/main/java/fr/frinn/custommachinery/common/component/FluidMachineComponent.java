package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IComparatorInputComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.component.ISerializableComponent;
import fr.frinn.custommachinery.api.component.ISideConfigComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.network.syncable.FluidStackSyncable;
import fr.frinn.custommachinery.common.network.syncable.SideConfigSyncable;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class FluidMachineComponent extends AbstractMachineComponent implements ISerializableComponent, ISyncableStuff, IComparatorInputComponent, ISideConfigComponent, IFluidHandler {

    private final String id;
    private final int capacity;
    private final int maxInput;
    private final int maxOutput;
    private final List<IIngredient<Fluid>> filter;
    private final boolean whitelist;
    private final SideConfig config;
    private final boolean unique;

    private FluidStack fluidStack = FluidStack.EMPTY;
    private boolean bypassLimit = false;

    public FluidMachineComponent(IMachineComponentManager manager, ComponentIOMode mode, String id, int capacity, int maxInput, int maxOutput, List<IIngredient<Fluid>> filter, boolean whitelist, SideConfig.Template configTemplate, boolean unique) {
        super(manager, mode);
        this.id = id;
        this.capacity = capacity;
        this.maxInput = maxInput;
        this.maxOutput = maxOutput;
        this.filter = filter;
        this.whitelist = whitelist;
        this.config = configTemplate.build(this);
        this.unique = unique;
    }

    @Override
    public MachineComponentType<FluidMachineComponent> getType() {
        return Registration.FLUID_MACHINE_COMPONENT.get();
    }

    public String getId() {
        return this.id;
    }

    public FluidStack getFluid() {
        return this.fluidStack;
    }

    public void setFluidStack(FluidStack fluidStack) {
        this.fluidStack = fluidStack.copy();
        getManager().markDirty();
    }

    public int getCapacity() {
        return this.capacity;
    }

    public int getMaxInput() {
        return this.maxInput;
    }

    public int getMaxOutput() {
        return this.maxOutput;
    }

    @Override
    public SideConfig getConfig() {
        return this.config;
    }

    @Override
    public void serialize(CompoundTag nbt, HolderLookup.Provider registries) {
        if(!this.fluidStack.isEmpty())
            nbt.put("stack", this.fluidStack.save(registries));
        nbt.put("config", this.config.serialize());
    }

    @Override
    public void deserialize(CompoundTag nbt, HolderLookup.Provider registries) {
        if(nbt.contains("stack", Tag.TAG_COMPOUND))
            this.fluidStack = FluidStack.parse(registries, nbt.getCompound("stack")).orElse(FluidStack.EMPTY);
        if(nbt.contains("config"))
            this.config.deserialize(nbt.getCompound("config"));
    }

    @Override
    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        container.accept(FluidStackSyncable.create(() -> this.fluidStack, fluidStack -> this.fluidStack = fluidStack));
        container.accept(SideConfigSyncable.create(this::getConfig, this.config::set));
    }

    @Override
    public int getComparatorInput() {
        return (int) (15 * ((double)this.fluidStack.getAmount() / (double)this.capacity));
    }

    /** FLUID HANDLER STUFF **/

    public int fillBypassLimit(FluidStack resource, FluidAction action) {
        this.bypassLimit = true;
        int filled = this.fill(resource, action);
        this.bypassLimit = false;
        return filled;
    }

    public FluidStack drainBypassLimit(int amount, FluidAction action) {
        this.bypassLimit = true;
        FluidStack drained = this.drain(amount, action);
        this.bypassLimit = false;
        return drained;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        validateTankIndex(tank);
        return this.getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        validateTankIndex(tank);
        return this.getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        validateTankIndex(tank);
        //Check unique
        if(this.unique && this.fluidStack.isEmpty() && this.getManager()
                .getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .stream()
                .flatMap(handler -> handler.getComponents().stream())
                .anyMatch(component -> component != this && FluidStack.isSameFluidSameComponents(component.getFluid(), stack)))
            return false;

        //Check filter
        if(this.filter.stream().anyMatch(ingredient -> ingredient.test(stack.getFluid())) != this.whitelist)
            return false;

        //Check if same fluid
        return this.fluidStack.isEmpty() || FluidStack.isSameFluidSameComponents(stack, this.fluidStack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !this.isFluidValid(0, resource))
            return 0;

        int maxFill = resource.getAmount();

        if(!this.bypassLimit)
            maxFill = Math.min(maxFill, this.getMaxInput());

        if(this.fluidStack.isEmpty()) {
            maxFill = Math.min(maxFill, this.getCapacity());
            if(action.execute())
                this.setFluidStack(resource.copyWithAmount(maxFill));
        } else {
            maxFill = Math.min(maxFill, this.getCapacity() - this.getFluid().getAmount());
            if(action.execute()) {
                this.fluidStack.grow(maxFill);
                getManager().markDirty();
            }
        }
        return maxFill;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if(maxDrain <= 0 || this.fluidStack.isEmpty())
            return FluidStack.EMPTY;

        if(!this.bypassLimit)
            maxDrain = Math.min(maxDrain, this.getMaxOutput());

        maxDrain = Math.min(maxDrain, this.fluidStack.getAmount());

        FluidStack removed = this.fluidStack.copyWithAmount(maxDrain);
        if(action.execute()) {
            this.fluidStack.shrink(maxDrain);
            getManager().markDirty();
        }
        return removed;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if(resource.isEmpty() || this.fluidStack.isEmpty() || !FluidStack.isSameFluidSameComponents(resource, this.fluidStack))
            return FluidStack.EMPTY;
        return this.drain(resource.getAmount(), action);
    }

    protected void validateTankIndex(int tank) {
        if(tank != 0)
            throw new RuntimeException("Tank " + tank + " not in valid range - [0," + this.getTanks() + ")");
    }

    /** Recipe Stuff **/

    public int getRecipeRemainingSpace() {
        if(!this.fluidStack.isEmpty())
            return this.capacity - this.fluidStack.getAmount();
        return this.capacity;
    }

    public void recipeInsert(Fluid fluid, long amount, CompoundTag nbt) {
        if(amount <= 0)
            return;

        if(this.fluidStack.isEmpty())
            this.fluidStack = new FluidStack(fluid, (int)amount);
        else {
            amount = Utils.clamp(amount, 0, getRecipeRemainingSpace());
            this.fluidStack.grow((int)amount);
        }
        getManager().markDirty();
    }

    public void recipeExtract(long amount) {
        if(amount <= 0)
            return;

        amount = Utils.clamp(amount, 0, this.fluidStack.getAmount());
        this.fluidStack.shrink((int)amount);
        getManager().markDirty();
    }

    public record Template(
            String id,
            int capacity,
            int maxInput,
            int maxOutput,
            List<IIngredient<Fluid>> filter,
            boolean whitelist,
            ComponentIOMode mode,
            SideConfig.Template config,
            boolean unique
    ) implements IMachineComponentTemplate<FluidMachineComponent> {

        public static final NamedCodec<FluidMachineComponent.Template> CODEC = NamedCodec.record(fluidMachineComponentTemplate ->
                fluidMachineComponentTemplate.group(
                        NamedCodec.STRING.fieldOf("id").forGetter(template -> template.id),
                        NamedCodec.intRange(1, Integer.MAX_VALUE).fieldOf("capacity").forGetter(template -> template.capacity),
                        NamedCodec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("maxInput").forGetter(template -> template.maxInput == template.capacity ? Optional.empty() : Optional.of(template.maxInput)),
                        NamedCodec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("maxOutput").forGetter(template -> template.maxOutput == template.capacity ? Optional.empty() : Optional.of(template.maxOutput)),
                        IIngredient.FLUID.listOf().optionalFieldOf("filter", Collections.emptyList()).forGetter(template -> template.filter),
                        NamedCodec.BOOL.optionalFieldOf("whitelist", false).forGetter(template -> template.whitelist),
                        ComponentIOMode.CODEC.optionalFieldOf("mode", ComponentIOMode.BOTH).forGetter(template -> template.mode),
                        SideConfig.Template.CODEC.optionalFieldOf("config").forGetter(template -> template.config == template.mode.getBaseConfig() ? Optional.empty() : Optional.of(template.config)),
                        NamedCodec.BOOL.optionalFieldOf("unique", false).forGetter(template -> template.unique)
                ).apply(fluidMachineComponentTemplate, (id, capacity, maxInput, maxOutput, filter, whitelist, mode, config, unique) ->
                        new Template(id, capacity, maxInput.orElse(capacity), maxOutput.orElse(capacity), filter, whitelist, mode, config.orElse(mode.getBaseConfig()), unique)
                ), "Fluid machine component"
        );

        @Override
        public MachineComponentType<FluidMachineComponent> getType() {
            return Registration.FLUID_MACHINE_COMPONENT.get();
        }

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public boolean canAccept(Object ingredient, boolean isInput, IMachineComponentManager manager) {
            if(this.mode != ComponentIOMode.BOTH && isInput != this.mode.isInput())
                return false;
            if(ingredient instanceof FluidStack stack) {
                return this.filter.stream().flatMap(f -> f.getAll().stream()).anyMatch(f -> f == stack.getFluid()) == this.whitelist;
            } else if(ingredient instanceof List<?> list) {
                return list.stream().allMatch(object -> {
                    if(object instanceof FluidStack stack)
                        return this.filter.stream().flatMap(f -> f.getAll().stream()).anyMatch(f -> f == stack.getFluid()) == this.whitelist;
                    return false;
                });
            }
            return false;
        }

        @Override
        public FluidMachineComponent build(IMachineComponentManager manager) {
            return new FluidMachineComponent(manager, this.mode, this.id, this.capacity, this.maxInput, this.maxOutput, this.filter, this.whitelist, this.config, this.unique);
        }
    }
}

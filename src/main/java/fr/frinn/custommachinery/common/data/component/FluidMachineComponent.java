package fr.frinn.custommachinery.common.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.component.*;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.api.utils.CodecLogger;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import fr.frinn.custommachinery.impl.network.syncable.FluidStackSyncable;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FluidMachineComponent extends AbstractMachineComponent implements ISerializableComponent, ISyncableStuff, IComparatorInputComponent, IFilterComponent {

    private String id;
    private int capacity;
    private int maxInput;
    private int maxOutput;
    private long actualTick;
    private int actualTickInput;
    private int actualTickOutput;
    private List<IIngredient<Fluid>> filter;
    private boolean whitelist;
    private FluidStack fluidStack = FluidStack.EMPTY;

    public FluidMachineComponent(IMachineComponentManager manager, ComponentIOMode mode, String id, int capacity, int maxInput, int maxOutput, List<IIngredient<Fluid>> filter, boolean whitelist) {
        super(manager, mode);
        this.id = id;
        this.capacity = capacity;
        this.maxInput = maxInput;
        this.maxOutput = maxOutput;
        this.filter = filter;
        this.whitelist = whitelist;
    }

    @Override
    public MachineComponentType<FluidMachineComponent> getType() {
        return Registration.FLUID_MACHINE_COMPONENT.get();
    }

    public String getId() {
        return this.id;
    }

    public int getMaxInput() {
        return this.maxInput;
    }

    public int getMaxOutput() {
        return this.maxOutput;
    }

    @Override
    public void serialize(CompoundNBT nbt) {
        nbt.putString("id", this.id);
        fluidStack.writeToNBT(nbt);
    }

    @Override
    public void deserialize(CompoundNBT nbt) {
        this.fluidStack = FluidStack.loadFluidStackFromNBT(nbt);
    }

    @Override
    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        container.accept(FluidStackSyncable.create(() -> this.fluidStack, fluidStack -> this.fluidStack = fluidStack));
    }

    @Override
    public int getComparatorInput() {
        return (int) (15 * ((double)this.fluidStack.getAmount() / (double)this.capacity));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Predicate<Object> getFilter() {
        return object -> {
            if(object instanceof FluidStack) {
                return isFluidValid((FluidStack)object);
            }
            else if(object instanceof List) {
                return ((List<Object>)object).stream().allMatch(o -> o instanceof FluidStack && isFluidValid((FluidStack)o));
            }
            else return false;
        };
    }

    /** FLUID HANDLER STUFF **/

    public FluidStack getFluidStack() {
        return this.fluidStack.copy();
    }

    public int getCapacity() {
        return this.capacity;
    }

    public int getRemainingSpace() {
        if(getManager().getTile().getWorld() == null)
            return 0;

        if(this.actualTick != this.getManager().getTile().getWorld().getGameTime()) {
            this.actualTick = this.getManager().getTile().getWorld().getGameTime();
            this.actualTickInput = 0;
            this.actualTickOutput = 0;
        }

        int maxInput = this.maxInput - this.actualTickInput;

        if(!this.fluidStack.isEmpty())
            return Math.min(this.capacity - this.fluidStack.getAmount(), maxInput);
        return Math.min(this.capacity, maxInput);
    }

    public boolean isFluidValid(@Nonnull FluidStack stack) {
        return this.filter.stream().anyMatch(ingredient -> ingredient.test(stack.getFluid())) == this.whitelist && (this.fluidStack.getFluid() == stack.getFluid() || this.fluidStack.isEmpty());
    }

    public int insert(Fluid fluid, int amount, FluidAction action) {
        if (amount <= 0 || getManager().getTile().getWorld() == null)
            return 0;

        if(this.actualTick != this.getManager().getTile().getWorld().getGameTime()) {
            this.actualTick = this.getManager().getTile().getWorld().getGameTime();
            this.actualTickInput = 0;
            this.actualTickOutput = 0;
        }

        int maxInsert = this.maxInput - this.actualTickInput;
        if(this.fluidStack.isEmpty()) {
            amount = Math.min(amount, maxInsert);
            if(action.execute()) {
                this.fluidStack = new FluidStack(fluid, amount);
                this.actualTickInput += amount;
            }
        }
        else {
            amount = Math.min(Math.min(getRemainingSpace(), maxInsert), amount);
            if(action.execute()) {
                this.fluidStack.grow(amount);
                this.actualTickInput += amount;
            }
        }
        return amount;
    }

    public FluidStack extract(int amount, FluidAction action) {
        if(amount <= 0 || getManager().getTile().getWorld() == null)
            return FluidStack.EMPTY;

        if(this.actualTick != this.getManager().getTile().getWorld().getGameTime()) {
            this.actualTick = this.getManager().getTile().getWorld().getGameTime();
            this.actualTickInput = 0;
            this.actualTickOutput = 0;
        }

        int maxExtract = this.maxOutput - this.actualTickOutput;
        amount = MathHelper.clamp(amount, 0, Math.min(this.fluidStack.getAmount(), maxExtract));
        if(action.execute()) {
            this.fluidStack.shrink(amount);
            this.actualTickOutput += amount;
        }
        return new FluidStack(this.fluidStack.getFluid(), amount);
    }

    /** Recipe Stuff **/

    public int getRecipeRemainingSpace() {
        if(!this.fluidStack.isEmpty())
            return this.capacity - this.fluidStack.getAmount();
        return this.capacity;
    }

    public void recipeInsert(Fluid fluid, int amount) {
        if(amount <= 0)
            return;

        if(this.fluidStack.isEmpty())
            this.fluidStack = new FluidStack(fluid, amount);
        else {
            amount = MathHelper.clamp(amount, 0, getRecipeRemainingSpace());
            this.fluidStack.grow(amount);
        }
    }

    public void recipeExtract(int amount) {
        if(amount <= 0)
            return;

        amount = MathHelper.clamp(amount, 0, this.fluidStack.getAmount());
        this.fluidStack.shrink(amount);
    }

    public static class Template implements IMachineComponentTemplate<FluidMachineComponent> {

        public static final Codec<FluidMachineComponent.Template> CODEC = RecordCodecBuilder.create(fluidMachineComponentTemplate ->
                fluidMachineComponentTemplate.group(
                        Codec.STRING.fieldOf("id").forGetter(template -> template.id),
                        Codec.INT.fieldOf("capacity").forGetter(template -> template.capacity),
                        CodecLogger.loggedOptional(Codec.INT,"maxInput").forGetter(template -> Optional.of(template.maxInput)),
                        CodecLogger.loggedOptional(Codec.INT,"maxOutput").forGetter(template -> Optional.of(template.maxOutput)),
                        CodecLogger.loggedOptional(Codecs.list(IIngredient.FLUID),"filter", Collections.emptyList()).forGetter(template -> template.filter),
                        CodecLogger.loggedOptional(Codec.BOOL,"whitelist", false).forGetter(template -> template.whitelist),
                        CodecLogger.loggedOptional(Codecs.COMPONENT_MODE_CODEC,"mode", ComponentIOMode.BOTH).forGetter(template -> template.mode)
                ).apply(fluidMachineComponentTemplate, (id, capacity, maxInput, maxOutput, filter, whitelist, mode) ->
                        new Template(id, capacity, maxInput.orElse(capacity), maxOutput.orElse(capacity), filter, whitelist, mode)
                )
        );

        private String id;
        private int capacity;
        private int maxInput;
        private int maxOutput;
        private List<IIngredient<Fluid>> filter;
        private boolean whitelist;
        private ComponentIOMode mode;

        public Template(String id, int capacity, int maxInput, int maxOutput, List<IIngredient<Fluid>> filter, boolean whitelist, ComponentIOMode mode) {
            this.id = id;
            this.capacity = capacity;
            this.maxInput = maxInput;
            this.maxOutput = maxOutput;
            this.filter = filter;
            this.whitelist = whitelist;
            this.mode = mode;
        }

        @Override
        public MachineComponentType<FluidMachineComponent> getType() {
            return Registration.FLUID_MACHINE_COMPONENT.get();
        }

        @Override
        public FluidMachineComponent build(IMachineComponentManager manager) {
            return new FluidMachineComponent(manager, this.mode, this.id, this.capacity, this.maxInput, this.maxOutput, this.filter, this.whitelist);
        }
    }
}

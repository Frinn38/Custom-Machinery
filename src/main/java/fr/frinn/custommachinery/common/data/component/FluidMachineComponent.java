package fr.frinn.custommachinery.common.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.common.init.Registration;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FluidMachineComponent extends AbstractMachineComponent {

    private String id;
    private int capacity;
    private int maxInput;
    private int maxOutput;
    private long actualTick;
    private int actualTickInput;
    private int actualTickOutput;
    private List<Fluid> filter;
    private FluidStack fluidStack = FluidStack.EMPTY;

    public FluidMachineComponent(MachineComponentManager manager, Mode mode, String id, int capacity, int maxInput, int maxOutput, List<Fluid> filter) {
        super(manager, mode);
        this.id = id;
        this.capacity = capacity;
        this.maxInput = maxInput;
        this.maxOutput = maxOutput;
        this.filter = filter;
    }

    @Override
    public MachineComponentType<FluidMachineComponent> getType() {
        return Registration.FLUID_MACHINE_COMPONENT.get();
    }

    public String getId() {
        return this.id;
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

    /** FLUID HANDLER STUFF **/

    public FluidStack getFluidStack() {
        return this.fluidStack.copy();
    }

    public int getCapacity() {
        return this.capacity;
    }

    public int getRemainingSpace() {
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
        return (this.filter.isEmpty() || this.filter.contains(stack.getFluid())) && (this.fluidStack.isFluidEqual(stack) || this.fluidStack.isEmpty());
    }

    public int insert(Fluid fluid, int amount, FluidAction action) {
        if (amount <= 0)
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
        if(amount <= 0)
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

        @SuppressWarnings("deprecation")
        public static final Codec<FluidMachineComponent.Template> CODEC = RecordCodecBuilder.create(fluidMachineComponentTemplate ->
                fluidMachineComponentTemplate.group(
                        Codec.STRING.fieldOf("id").forGetter(template -> template.id),
                        Codec.INT.fieldOf("capacity").forGetter(template -> template.capacity),
                        Codec.INT.optionalFieldOf("maxInput").forGetter(template -> Optional.of(template.maxInput)),
                        Codec.INT.optionalFieldOf("maxOutput").forGetter(template -> Optional.of(template.maxOutput)),
                        Registry.FLUID.listOf().optionalFieldOf("filter", new ArrayList<>()).forGetter(template -> template.filter),
                        Mode.CODEC.optionalFieldOf("mode", Mode.BOTH).forGetter(template -> template.mode)
                ).apply(fluidMachineComponentTemplate, (id, capacity, maxInput, maxOutput, filter, mode) ->
                        new Template(id, capacity, maxInput.orElse(capacity), maxOutput.orElse(capacity), filter, mode)
                )
        );

        private String id;
        private int capacity;
        private int maxInput;
        private int maxOutput;
        private List<Fluid> filter;
        private Mode mode;

        private Template(String id, int capacity, int maxInput, int maxOutput, List<Fluid> filter, Mode mode) {
            this.id = id;
            this.capacity = capacity;
            this.maxInput = maxInput;
            this.maxOutput = maxOutput;
            this.filter = filter;
            this.mode = mode;
        }

        @Override
        public MachineComponentType<FluidMachineComponent> getType() {
            return Registration.FLUID_MACHINE_COMPONENT.get();
        }

        @Override
        public FluidMachineComponent build(MachineComponentManager manager) {
            return new FluidMachineComponent(manager, mode, id, capacity, maxInput, maxOutput, filter);
        }
    }
}

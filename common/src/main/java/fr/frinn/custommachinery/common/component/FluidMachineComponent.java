package fr.frinn.custommachinery.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.fluid.FluidStack;
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
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import fr.frinn.custommachinery.impl.codec.CodecLogger;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class FluidMachineComponent extends AbstractMachineComponent implements ISerializableComponent, ISyncableStuff, IComparatorInputComponent, ISideConfigComponent {

    private final String id;
    private final long capacity;
    private final long maxInput;
    private final long maxOutput;
    private final List<IIngredient<Fluid>> filter;
    private final boolean whitelist;
    private final SideConfig config;

    private FluidStack fluidStack = FluidStack.empty();

    public FluidMachineComponent(IMachineComponentManager manager, ComponentIOMode mode, String id, long capacity, long maxInput, long maxOutput, List<IIngredient<Fluid>> filter, boolean whitelist, SideConfig.Template configTemplate) {
        super(manager, mode);
        this.id = id;
        this.capacity = capacity;
        this.maxInput = maxInput;
        this.maxOutput = maxOutput;
        this.filter = filter;
        this.whitelist = whitelist;
        this.config = configTemplate.build(this);
    }

    @Override
    public MachineComponentType<FluidMachineComponent> getType() {
        return Registration.FLUID_MACHINE_COMPONENT.get();
    }

    public String getId() {
        return this.id;
    }

    public long getMaxInput() {
        return this.maxInput;
    }

    public long getMaxOutput() {
        return this.maxOutput;
    }

    @Override
    public SideConfig getConfig() {
        return this.config;
    }

    @Override
    public void serialize(CompoundTag nbt) {
        if(!this.fluidStack.isEmpty())
            nbt.put("stack", this.fluidStack.write(new CompoundTag()));
        nbt.put("config", this.config.serialize());
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        if(nbt.contains("stack", Tag.TAG_COMPOUND))
            this.fluidStack = FluidStack.read(nbt.getCompound("stack"));
        if(nbt.contains("config"))
            this.config.deserialize(nbt.get("config"));
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

    public FluidStack getFluidStack() {
        return this.fluidStack.copy();
    }

    public void setFluidStack(FluidStack fluidStack) {
        this.fluidStack = fluidStack.copy();
        getManager().markDirty();
    }

    public long getCapacity() {
        return this.capacity;
    }

    public long getRemainingSpace() {
        if(!this.fluidStack.isEmpty())
            return Math.min(this.capacity - this.fluidStack.getAmount(), getMaxInput());
        return Math.min(this.capacity, getMaxInput());
    }

    public boolean isFluidValid(@Nonnull FluidStack stack) {
        return this.filter.stream().anyMatch(ingredient -> ingredient.test(stack.getFluid())) == this.whitelist && (this.fluidStack.isEmpty() || stack.isFluidEqual(this.fluidStack));
    }

    public long insert(Fluid fluid, long amount, CompoundTag nbt, boolean simulate) {
        if (amount <= 0)
            return 0;

        if(this.fluidStack.isEmpty()) {
            amount = Math.min(amount, getMaxInput());
            if(!simulate) {
                this.fluidStack = FluidStack.create(fluid, amount, nbt);
                getManager().markDirty();
            }
        }
        else {
            amount = Math.min(Math.min(getRemainingSpace(), getMaxInput()), amount);
            if(!simulate) {
                this.fluidStack.grow(amount);
                getManager().markDirty();
            }
        }
        return amount;
    }

    public FluidStack extract(long amount, boolean simulate) {
        if(amount <= 0 || this.fluidStack.isEmpty())
            return FluidStack.empty();

        amount = Mth.clamp(amount, 0, Math.min(this.fluidStack.getAmount(), getMaxOutput()));
        FluidStack removed = FluidStack.create(this.fluidStack.getFluid(), amount, this.fluidStack.getTag());
        if(!simulate) {
            this.fluidStack.shrink(amount);
            getManager().markDirty();
        }
        return removed;
    }

    /** Recipe Stuff **/

    public long getRecipeRemainingSpace() {
        if(!this.fluidStack.isEmpty())
            return this.capacity - this.fluidStack.getAmount();
        return this.capacity;
    }

    public void recipeInsert(Fluid fluid, long amount, CompoundTag nbt) {
        if(amount <= 0)
            return;

        if(this.fluidStack.isEmpty())
            this.fluidStack = FluidStack.create(fluid, amount, nbt);
        else {
            amount = Mth.clamp(amount, 0, getRecipeRemainingSpace());
            this.fluidStack.grow(amount);
        }
        getManager().markDirty();
    }

    public void recipeExtract(long amount) {
        if(amount <= 0)
            return;

        amount = Mth.clamp(amount, 0, this.fluidStack.getAmount());
        this.fluidStack.shrink(amount);
        getManager().markDirty();
    }

    public static class Template implements IMachineComponentTemplate<FluidMachineComponent> {

        public static final Codec<FluidMachineComponent.Template> CODEC = RecordCodecBuilder.create(fluidMachineComponentTemplate ->
                fluidMachineComponentTemplate.group(
                        Codec.STRING.fieldOf("id").forGetter(template -> template.id),
                        Codec.LONG.fieldOf("capacity").forGetter(template -> template.capacity),
                        CodecLogger.loggedOptional(Codec.LONG,"maxInput").forGetter(template -> Optional.of(template.maxInput)),
                        CodecLogger.loggedOptional(Codec.LONG,"maxOutput").forGetter(template -> Optional.of(template.maxOutput)),
                        CodecLogger.loggedOptional(Codecs.list(IIngredient.FLUID),"filter", Collections.emptyList()).forGetter(template -> template.filter),
                        CodecLogger.loggedOptional(Codec.BOOL,"whitelist", false).forGetter(template -> template.whitelist),
                        CodecLogger.loggedOptional(Codecs.COMPONENT_MODE_CODEC,"mode", ComponentIOMode.BOTH).forGetter(template -> template.mode),
                        CodecLogger.loggedOptional(SideConfig.Template.CODEC, "config").forGetter(template -> Optional.of(template.config))
                ).apply(fluidMachineComponentTemplate, (id, capacity, maxInput, maxOutput, filter, whitelist, mode, config) ->
                        new Template(id, capacity, maxInput.orElse(capacity), maxOutput.orElse(capacity), filter, whitelist, mode, config.orElse(mode.getBaseConfig()))
                )
        );

        private final String id;
        private final long capacity;
        private final long maxInput;
        private final long maxOutput;
        private final List<IIngredient<Fluid>> filter;
        private final boolean whitelist;
        private final ComponentIOMode mode;
        private final SideConfig.Template config;

        public Template(String id, long capacity, long maxInput, long maxOutput, List<IIngredient<Fluid>> filter, boolean whitelist, ComponentIOMode mode, SideConfig.Template config) {
            this.id = id;
            this.capacity = capacity;
            this.maxInput = maxInput;
            this.maxOutput = maxOutput;
            this.filter = filter;
            this.whitelist = whitelist;
            this.mode = mode;
            this.config = config;
        }

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
            if(isInput != this.mode.isInput())
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
            return new FluidMachineComponent(manager, this.mode, this.id, this.capacity, this.maxInput, this.maxOutput, this.filter, this.whitelist, this.config);
        }
    }
}

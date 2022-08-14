package fr.frinn.custommachinery.common.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.ITickableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.apiimpl.codec.CodecLogger;
import fr.frinn.custommachinery.apiimpl.requirement.AbstractChanceableRequirement;
import fr.frinn.custommachinery.client.integration.jei.wrapper.FluidIngredientWrapper;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.ingredient.FluidTagIngredient;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FluidPerTickRequirement extends AbstractChanceableRequirement<FluidComponentHandler> implements ITickableRequirement<FluidComponentHandler>, IJEIIngredientRequirement<FluidStack> {

    public static final Codec<FluidPerTickRequirement> CODEC = RecordCodecBuilder.create(fluidPerTickRequirementInstance ->
            fluidPerTickRequirementInstance.group(
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(IRequirement::getMode),
                    IIngredient.FLUID.fieldOf("fluid").forGetter(requirement -> requirement.fluid),
                    Codec.INT.fieldOf("amount").forGetter(requirement -> requirement.amount),
                    CodecLogger.loggedOptional(Codec.doubleRange(0.0, 1.0),"chance", 1.0D).forGetter(AbstractChanceableRequirement::getChance),
                    CodecLogger.loggedOptional(Codecs.COMPOUND_NBT_CODEC, "nbt").forGetter(requirement -> Optional.ofNullable(requirement.nbt)),
                    CodecLogger.loggedOptional(Codec.STRING,"tank", "").forGetter(requirement -> requirement.tank)
            ).apply(fluidPerTickRequirementInstance, (mode, fluid, amount, chance, nbt, tank) -> {
                    FluidPerTickRequirement requirement = new FluidPerTickRequirement(mode, fluid, amount, nbt.orElse(null), tank);
                    requirement.setChance(chance);
                    return requirement;
            })
    );

    private final IIngredient<Fluid> fluid;
    private final int amount;
    @Nullable
    private final CompoundTag nbt;
    private final String tank;

    public FluidPerTickRequirement(RequirementIOMode mode, IIngredient<Fluid> fluid, int amount, @Nullable CompoundTag nbt, String tank) {
        super(mode);
        if(mode == RequirementIOMode.OUTPUT && fluid instanceof FluidTagIngredient)
            throw new IllegalArgumentException("You can't use a tag for an Output Fluid Per Tick Requirement");
        this.fluid = fluid;
        this.amount = amount;
        this.nbt = nbt;
        this.tank = tank;
    }

    @Override
    public RequirementType<FluidPerTickRequirement> getType() {
        return Registration.FLUID_PER_TICK_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType getComponentType() {
        return Registration.FLUID_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean test(FluidComponentHandler component, ICraftingContext context) {
        int amount = (int)context.getPerTickModifiedValue(this.amount, this, null);
        if(getMode() == RequirementIOMode.INPUT)
            return this.fluid.getAll().stream().mapToInt(fluid -> component.getFluidAmount(this.tank, fluid, this.nbt)).sum() >= amount;
        else {
            if(this.fluid.getAll().get(0) != null)
                return component.getSpaceForFluid(this.tank, this.fluid.getAll().get(0), this.nbt) >= amount;
            throw new IllegalStateException("Can't use output fluid per tick requirement with fluid tag");
        }
    }

    @Override
    public CraftingResult processStart(FluidComponentHandler component, ICraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processTick(FluidComponentHandler component, ICraftingContext context) {
        int amount = (int)context.getPerTickModifiedValue(this.amount, this, null);
        if(getMode() == RequirementIOMode.INPUT) {
            int maxDrain = this.fluid.getAll().stream().mapToInt(fluid -> component.getFluidAmount(this.tank, fluid, this.nbt)).sum();
            if(maxDrain >= amount) {
                int toDrain = amount;
                for (Fluid fluid : this.fluid.getAll()) {
                    int canDrain = component.getFluidAmount(this.tank, fluid, this.nbt);
                    if(canDrain > 0) {
                        canDrain = Math.min(canDrain, toDrain);
                        component.removeFromInputs(this.tank, new FluidStack(fluid, canDrain, this.nbt));
                        toDrain -= canDrain;
                        if(toDrain == 0)
                            return CraftingResult.success();
                    }
                }
            }
            return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.fluid.error.input", this.fluid, amount, maxDrain));
        } else {
            if(this.fluid.getAll().get(0) != null) {
                Fluid fluid = this.fluid.getAll().get(0);
                FluidStack stack = new FluidStack(fluid, amount, this.nbt);
                int canInsert = component.getSpaceForFluid(this.tank, fluid, this.nbt);
                if(canInsert >= amount) {
                    component.addToOutputs(this.tank, stack);
                    return CraftingResult.success();
                }
                return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.fluidpertick.error.output", amount, new TranslatableComponent(fluid.getAttributes().getTranslationKey())));
            } else throw new IllegalStateException("Can't use fluid per tick requirement with fluid tag");
        }
    }

    @Override
    public CraftingResult processEnd(FluidComponentHandler component, ICraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public List<IJEIIngredientWrapper<FluidStack>> getJEIIngredientWrappers() {
        return Collections.singletonList(new FluidIngredientWrapper(this.getMode(), this.fluid, this.amount, getChance(), true, this.nbt, this.tank));
    }
}

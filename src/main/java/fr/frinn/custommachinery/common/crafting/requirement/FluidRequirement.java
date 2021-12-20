package fr.frinn.custommachinery.common.crafting.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.codec.CodecLogger;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.api.requirement.IChanceableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.apiimpl.requirement.AbstractRequirement;
import fr.frinn.custommachinery.common.data.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.wrapper.FluidIngredientWrapper;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.ingredient.FluidTagIngredient;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

public class FluidRequirement extends AbstractRequirement<FluidComponentHandler> implements IChanceableRequirement<FluidComponentHandler>, IJEIIngredientRequirement<FluidStack> {

    public static final Codec<FluidRequirement> CODEC = RecordCodecBuilder.create(fluidRequirementInstance ->
            fluidRequirementInstance.group(
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(AbstractRequirement::getMode),
                    IIngredient.FLUID.fieldOf("fluid").forGetter(requirement -> requirement.fluid),
                    Codec.INT.fieldOf("amount").forGetter(requirement -> requirement.amount),
                    CodecLogger.loggedOptional(Codec.doubleRange(0.0, 1.0),"chance", 1.0D).forGetter(requirement -> requirement.chance),
                    CodecLogger.loggedOptional(Codecs.COMPOUND_NBT_CODEC, "nbt").forGetter(requirement -> Optional.ofNullable(requirement.nbt)),
                    CodecLogger.loggedOptional(Codec.STRING,"tank", "").forGetter(requirement -> requirement.tank)
            ).apply(fluidRequirementInstance, (mode, fluid, amount, chance, nbt, tank) -> {
                    FluidRequirement requirement = new FluidRequirement(mode, fluid, amount, nbt.orElse(null), tank);
                    requirement.setChance(chance);
                    return requirement;
            })
    );

    private final IIngredient<Fluid> fluid;
    private final int amount;
    private double chance = 1.0D;
    @Nullable
    private final CompoundNBT nbt;
    private final String tank;
    private final Lazy<FluidIngredientWrapper> wrapper;

    public FluidRequirement(RequirementIOMode mode, IIngredient<Fluid> fluid, int amount, @Nullable CompoundNBT nbt, String tank) {
        super(mode);
        if(mode == RequirementIOMode.OUTPUT && fluid instanceof FluidTagIngredient)
            throw new IllegalArgumentException("You must specify a fluid for an Output Fluid Requirement");
        this.fluid = fluid;
        this.amount = amount;
        this.nbt = nbt;
        this.tank = tank;
        this.wrapper = Lazy.of(() -> new FluidIngredientWrapper(this.getMode(), this.fluid, this.amount, this.chance, false, this.nbt, this.tank));
    }

    @Override
    public RequirementType<FluidRequirement> getType() {
        return Registration.FLUID_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType getComponentType() {
        return Registration.FLUID_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean test(FluidComponentHandler component, ICraftingContext context) {
        int amount = (int)context.getModifiedValue(this.amount, this, null);
        if(getMode() == RequirementIOMode.INPUT) {
            return this.fluid.getAll().stream().mapToInt(fluid -> component.getFluidAmount(this.tank, fluid, this.nbt)).sum() >= amount;
        }
        else {
            if(this.fluid.getAll().get(0) != null)
                return component.getSpaceForFluid(this.tank, this.fluid.getAll().get(0), this.nbt) >= amount;
            else throw new IllegalStateException("Can't use output fluid requirement with fluid tag");
        }
    }

    @Override
    public CraftingResult processStart(FluidComponentHandler component, ICraftingContext context) {
        int amount = (int)context.getModifiedValue(this.amount, this, null);
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
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.fluid.error.input", this.fluid, amount, maxDrain));
        }
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(FluidComponentHandler component, ICraftingContext context) {
        int amount = (int)context.getModifiedValue(this.amount, this, null);
        if(getMode() == RequirementIOMode.OUTPUT) {
            if(this.fluid.getAll().get(0) != null) {
                Fluid fluid = this.fluid.getAll().get(0);
                int canFill =  component.getSpaceForFluid(this.tank, fluid, this.nbt);
                if(canFill >= amount) {
                    FluidStack stack = new FluidStack(fluid, amount, this.nbt);
                    component.addToOutputs(this.tank, stack);
                    return CraftingResult.success();
                }
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.fluid.error.output", amount, new TranslationTextComponent(fluid.getAttributes().getTranslationKey())));
            } else throw new IllegalStateException("Can't use output fluid requirement with fluid tag");
        }
        return CraftingResult.pass();
    }

    @Override
    public void setChance(double chance) {
        this.chance = MathHelper.clamp(chance, 0.0, 1.0);
    }

    @Override
    public boolean shouldSkip(FluidComponentHandler component, Random rand, ICraftingContext context) {
        double chance = context.getModifiedValue(this.chance, this, "chance");
        return rand.nextDouble() > chance;
    }

    @Override
    public FluidIngredientWrapper getJEIIngredientWrapper() {
        return this.wrapper.get();
    }
}

package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.components.MachineComponentType;
import fr.frinn.custommachinery.api.utils.CodecLogger;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.common.integration.jei.wrapper.FluidIngredientWrapper;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.Ingredient;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;

import java.util.Random;

public class FluidPerTickRequirement extends AbstractTickableRequirement<FluidComponentHandler> implements IChanceableRequirement<FluidComponentHandler>, IJEIIngredientRequirement {

    public static final Codec<FluidPerTickRequirement> CODEC = RecordCodecBuilder.create(fluidPerTickRequirementInstance ->
            fluidPerTickRequirementInstance.group(
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(AbstractTickableRequirement::getMode),
                    Ingredient.FluidIngredient.CODEC.fieldOf("fluid").forGetter(requirement -> requirement.fluid),
                    Codec.INT.fieldOf("amount").forGetter(requirement -> requirement.amount),
                    CodecLogger.loggedOptional(Codec.doubleRange(0.0, 1.0),"chance", 1.0D).forGetter(requirement -> requirement.chance),
                    CodecLogger.loggedOptional(Codec.STRING,"tank", "").forGetter(requirement -> requirement.tank)
            ).apply(fluidPerTickRequirementInstance, (mode, fluid, amount, chance, tank) -> {
                    FluidPerTickRequirement requirement = new FluidPerTickRequirement(mode, fluid, amount, tank);
                    requirement.setChance(chance);
                    return requirement;
            })
    );

    private Ingredient.FluidIngredient fluid;
    private int amount;
    private double chance = 1.0D;
    private String tank;

    public FluidPerTickRequirement(MODE mode, Ingredient.FluidIngredient fluid, int amount, String tank) {
        super(mode);
        if(mode == MODE.OUTPUT && fluid.getObject() == null)
            throw new IllegalArgumentException("You must specify a fluid for an Output Fluid Per Tick Requirement");
        this.fluid = fluid;
        this.amount = amount;
        this.tank = tank;
        this.fluidIngredientWrapper = new FluidIngredientWrapper(this.getMode(), this.fluid, this.amount, this.chance, true, this.tank);
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
    public boolean test(FluidComponentHandler component, CraftingContext context) {
        int amount = (int)context.getPerTickModifiedValue(this.amount, this, null);
        if(getMode() == MODE.INPUT)
            return this.fluid.getAll().stream().mapToInt(fluid -> component.getFluidAmount(this.tank, fluid)).sum() >= amount;
        else {
            if(this.fluid.getObject() != null)
                return component.getSpaceForFluid(this.tank, this.fluid.getObject()) >= amount;
            throw new IllegalStateException("Can't use output fluid per tick requirement with fluid tag");
        }
    }

    @Override
    public CraftingResult processStart(FluidComponentHandler component, CraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processTick(FluidComponentHandler component, CraftingContext context) {
        int amount = (int)context.getPerTickModifiedValue(this.amount, this, null);
        if(getMode() == MODE.INPUT) {
            int maxDrain = this.fluid.getAll().stream().mapToInt(fluid -> component.getFluidAmount(this.tank, fluid)).sum();
            if(maxDrain >= amount) {
                int toDrain = amount;
                for (Fluid fluid : this.fluid.getAll()) {
                    int canDrain = component.getFluidAmount(this.tank, fluid);
                    if(canDrain > 0) {
                        canDrain = Math.min(canDrain, toDrain);
                        component.removeFromInputs(this.tank, new FluidStack(fluid, canDrain));
                        toDrain -= canDrain;
                        if(toDrain == 0)
                            return CraftingResult.success();
                    }
                }
            }
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.fluid.error.input", this.fluid, amount, maxDrain));
        } else {
            if(this.fluid.getObject() != null) {
                Fluid fluid = this.fluid.getObject();
                FluidStack stack = new FluidStack(fluid, amount);
                int canInsert = component.getSpaceForFluid(this.tank, fluid);
                if(canInsert >= amount) {
                    component.addToOutputs(this.tank, stack);
                    return CraftingResult.success();
                }
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.fluidpertick.error.output", amount, new TranslationTextComponent(fluid.getAttributes().getTranslationKey())));
            } else throw new IllegalStateException("Can't use fluid per tick requirement with fluid tag");
        }
    }

    @Override
    public CraftingResult processEnd(FluidComponentHandler component, CraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public void setChance(double chance) {
        this.chance = MathHelper.clamp(chance, 0.0, 1.0);
    }

    @Override
    public boolean testChance(FluidComponentHandler component, Random rand, CraftingContext context) {
        double chance = context.getModifiedvalue(this.chance, this, "chance");
        return rand.nextDouble() > chance;
    }

    private FluidIngredientWrapper fluidIngredientWrapper;
    @Override
    public FluidIngredientWrapper getJEIIngredientWrapper() {
        return this.fluidIngredientWrapper;
    }
}

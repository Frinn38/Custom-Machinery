package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.client.integration.jei.wrapper.FluidIngredientWrapper;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.requirement.AbstractChanceableRequirement;
import fr.frinn.custommachinery.impl.requirement.AbstractRequirement;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FluidRequirement extends AbstractChanceableRequirement<FluidComponentHandler> implements IJEIIngredientRequirement<FluidStack> {

    public static final NamedCodec<FluidRequirement> CODEC = NamedCodec.record(fluidRequirementInstance ->
            fluidRequirementInstance.group(
                    RequirementIOMode.CODEC.fieldOf("mode").forGetter(AbstractRequirement::getMode),
                    NamedCodec.of(SizedFluidIngredient.FLAT_CODEC).fieldOf("ingredient").forGetter(requirement -> requirement.ingredient),
                    NamedCodec.doubleRange(0.0, 1.0).optionalFieldOf("chance", 1.0D).forGetter(AbstractChanceableRequirement::getChance),
                    NamedCodec.STRING.optionalFieldOf("tank", "").forGetter(requirement -> requirement.tank)
            ).apply(fluidRequirementInstance, (mode, fluid, chance, tank) -> {
                    FluidRequirement requirement = new FluidRequirement(mode, fluid, tank);
                    requirement.setChance(chance);
                    return requirement;
            }), "Fluid requirement"
    );

    private final SizedFluidIngredient ingredient;
    private final FluidStack output;
    private final String tank;

    public FluidRequirement(RequirementIOMode mode, SizedFluidIngredient ingredient, String tank) {
        super(mode);
        if(ingredient.ingredient().hasNoFluids())
            throw new IllegalArgumentException("Invalid fluid specified for fluid requirement");
        if(mode == RequirementIOMode.OUTPUT) {
            if(ingredient.getFluids().length > 1)
                throw new IllegalArgumentException("You must specify a single for an Output Fluid Requirement");
            else
                this.output = ingredient.getFluids()[0];
        } else
            this.output = FluidStack.EMPTY;
        this.ingredient = ingredient;
        this.tank = tank;
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
        int amount = (int)context.getIntegerModifiedValue(this.ingredient.amount(), this, null);
        if(getMode() == RequirementIOMode.INPUT) {
            return Arrays.stream(this.ingredient.getFluids()).mapToInt(fluid -> component.getFluidAmount(this.tank, fluid)).sum() >= amount;
        }
        else
            return component.getSpaceForFluid(this.tank, this.output) >= amount;
    }

    @Override
    public CraftingResult processStart(FluidComponentHandler component, ICraftingContext context) {
        int amount = (int)context.getIntegerModifiedValue(this.ingredient.amount(), this, null);
        if(getMode() == RequirementIOMode.INPUT) {
            int maxDrain = Arrays.stream(this.ingredient.getFluids()).mapToInt(fluid -> component.getFluidAmount(this.tank, fluid)).sum();
            if(maxDrain >= amount) {
                int toDrain = amount;
                for (FluidStack fluid : this.ingredient.getFluids()) {
                    int canDrain = component.getFluidAmount(this.tank, fluid);
                    if(canDrain > 0) {
                        canDrain = Math.min(canDrain, toDrain);
                        component.removeFromInputs(this.tank, fluid.copyWithAmount(canDrain));
                        toDrain -= canDrain;
                        if(toDrain == 0)
                            return CraftingResult.success();
                    }
                }
            }
            return CraftingResult.error(Component.translatable("custommachinery.requirements.fluid.error.input", this.ingredient.toString(), amount, maxDrain));
        }
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(FluidComponentHandler component, ICraftingContext context) {
        int amount = (int)context.getIntegerModifiedValue(this.ingredient.amount(), this, null);
        if(getMode() == RequirementIOMode.OUTPUT) {
            int canFill =  component.getSpaceForFluid(this.tank, this.output);
            if(canFill >= amount) {
                component.addToOutputs(this.tank, this.output.copyWithAmount(amount));
                return CraftingResult.success();
            }
            return CraftingResult.error(Component.translatable("custommachinery.requirements.fluid.error.output", amount, this.output.copyWithAmount(canFill).getHoverName()));
        }
        return CraftingResult.pass();
    }

    @Override
    public List<IJEIIngredientWrapper<FluidStack>> getJEIIngredientWrappers(IMachineRecipe recipe) {
        return Collections.singletonList(new FluidIngredientWrapper(this.getMode(), this.ingredient, getChance(), false, this.tank));
    }
}

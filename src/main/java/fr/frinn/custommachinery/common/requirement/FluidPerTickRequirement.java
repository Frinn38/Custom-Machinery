package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.crafting.IRequirementList;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RecipeRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.client.integration.jei.wrapper.FluidIngredientWrapper;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.Collections;
import java.util.List;

public record FluidPerTickRequirement(RequirementIOMode mode, SizedFluidIngredient ingredient, List<String> tanks) implements IRequirement<FluidComponentHandler>, IJEIIngredientRequirement<FluidStack> {

    public static final NamedCodec<FluidPerTickRequirement> CODEC = NamedCodec.record(fluidPerTickRequirementInstance ->
            fluidPerTickRequirementInstance.group(
                    RequirementIOMode.CODEC.fieldOf("mode").forGetter(IRequirement::getMode),
                    NamedCodec.of(SizedFluidIngredient.FLAT_CODEC).fieldOf("ingredient").forGetter(requirement -> requirement.ingredient),
                    NamedCodec.STRING.listOf().optionalFieldOf("tanks", Collections.emptyList()).aliases("tank").forGetter(requirement -> requirement.tanks)
            ).apply(fluidPerTickRequirementInstance, FluidPerTickRequirement::new), "Fluid per tick requirement"
    );

    public FluidPerTickRequirement(RequirementIOMode mode, SizedFluidIngredient ingredient, List<String> tanks) {
        this.mode = mode;
        if(ingredient.ingredient().hasNoFluids())
            throw new IllegalArgumentException("Invalid fluid specified for fluid requirement");
        if(mode == RequirementIOMode.OUTPUT && ingredient.getFluids().length > 1)
                throw new IllegalArgumentException("You must specify a single for an Output Fluid Requirement");
        this.ingredient = ingredient;
        this.tanks = tanks;
    }

    private FluidStack output() {
        return this.ingredient.getFluids()[0];
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
    public RequirementIOMode getMode() {
        return this.mode;
    }

    @Override
    public boolean test(FluidComponentHandler component, ICraftingContext context) {
        int amount = (int)context.getIntegerModifiedValue(this.ingredient.amount(), this, null);
        if(getMode() == RequirementIOMode.INPUT) {
            return component.getIngredientAmount(this.tanks, this.ingredient.ingredient()) >= amount;
        }
        else
            return component.getSpaceForFluid(this.tanks, this.output()) >= amount;
    }

    @Override
    public void gatherRequirements(IRequirementList<FluidComponentHandler> list) {
        if(this.mode == RequirementIOMode.INPUT)
            list.processEachTick(this::processInputs);
        else
            list.processEachTick(this::processOutputs);
    }

    private CraftingResult processInputs(FluidComponentHandler component, ICraftingContext context) {
        int amount = (int)context.getIntegerModifiedValue(this.ingredient.amount(), this, null);
        int maxExtract = component.getIngredientAmount(this.tanks, this.ingredient.ingredient());
        if(maxExtract >= amount) {
            component.removeFromInputs(this.tanks, this.ingredient.ingredient(), amount);
            return CraftingResult.success();
        }
        return CraftingResult.error(Component.translatable("custommachinery.requirements.fluid.error.input", Utils.fluidIngredientName(this.ingredient), amount, maxExtract));
    }

    private CraftingResult processOutputs(FluidComponentHandler component, ICraftingContext context) {
        int amount = (int)context.getPerTickIntegerModifiedValue(this.ingredient.amount(), this, null);
        int canFill =  component.getSpaceForFluid(this.tanks, this.output());
        if(canFill >= amount) {
            component.addToOutputs(this.tanks, this.output().copyWithAmount(amount));
            return CraftingResult.success();
        }
        return CraftingResult.error(Component.translatable("custommachinery.requirements.fluid.error.output", amount, this.output().getHoverName()));
    }

    @Override
    public List<IJEIIngredientWrapper<FluidStack>> getJEIIngredientWrappers(IMachineRecipe recipe, RecipeRequirement<?, ?> requirement) {
        return Collections.singletonList(new FluidIngredientWrapper(this.getMode(), this.ingredient, requirement.chance(), true, this.tanks));
    }
}

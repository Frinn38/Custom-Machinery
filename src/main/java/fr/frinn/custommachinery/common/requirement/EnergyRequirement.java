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
import fr.frinn.custommachinery.client.integration.jei.wrapper.EnergyIngredientWrapper;
import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.integration.jei.Energy;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public record EnergyRequirement(RequirementIOMode mode, int amount) implements IRequirement<EnergyMachineComponent>, IJEIIngredientRequirement<Energy> {

    public static final NamedCodec<EnergyRequirement> CODEC = NamedCodec.record(energyRequirementInstance ->
            energyRequirementInstance.group(
                    RequirementIOMode.CODEC.fieldOf("mode").forGetter(EnergyRequirement::getMode),
                    NamedCodec.INT.fieldOf("amount").forGetter(requirement -> requirement.amount)
            ).apply(energyRequirementInstance, (EnergyRequirement::new)), "Energy requirement"
    );

    @Override
    public RequirementType<EnergyRequirement> getType() {
        return Registration.ENERGY_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType<EnergyMachineComponent> getComponentType() {
        return Registration.ENERGY_MACHINE_COMPONENT.get();
    }

    @Override
    public RequirementIOMode getMode() {
        return this.mode;
    }

    @Override
    public boolean test(EnergyMachineComponent energy, ICraftingContext context) {
        int amount = (int)context.getIntegerModifiedValue(this.amount, this, null);
        if(getMode() == RequirementIOMode.INPUT)
            return energy.extractRecipeEnergy(amount, true) == amount;
        else
            return energy.receiveRecipeEnergy(amount, true) == amount;
    }

    @Override
    public void gatherRequirements(IRequirementList<EnergyMachineComponent> list) {
        if(this.mode == RequirementIOMode.INPUT)
            list.processOnStart(this::processInputs);
        else
            list.processOnEnd(this::processOutputs);
    }

    private CraftingResult processInputs(EnergyMachineComponent energy, ICraftingContext context) {
        int amount = (int)context.getIntegerModifiedValue(this.amount, this, null);
        int canExtract = energy.extractRecipeEnergy(amount, true);
        if(canExtract == amount) {
            energy.extractRecipeEnergy(amount, false);
            return CraftingResult.success();
        }
        return CraftingResult.error(Component.translatable("custommachinery.requirements.energy.error.input", amount, canExtract));
    }

    private CraftingResult processOutputs(EnergyMachineComponent energy, ICraftingContext context) {
        int amount = (int)context.getIntegerModifiedValue(this.amount, this, null);
        int canReceive = energy.receiveRecipeEnergy(amount, true);
        if(canReceive == amount) {
            energy.receiveRecipeEnergy(amount, false);
            return CraftingResult.success();
        }
        return CraftingResult.error(Component.translatable("custommachinery.requirements.energy.error.output", amount));
    }

    @Override
    public List<IJEIIngredientWrapper<Energy>> getJEIIngredientWrappers(IMachineRecipe recipe, RecipeRequirement<?, ?> requirement) {
        return Collections.singletonList(new EnergyIngredientWrapper(this.getMode(), this.amount, requirement.chance(), false, recipe.getRecipeTime()));
    }
}

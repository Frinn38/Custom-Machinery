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

public record EnergyPerTickRequirement(RequirementIOMode mode, int amount) implements IRequirement<EnergyMachineComponent>, IJEIIngredientRequirement<Energy> {

    public static final NamedCodec<EnergyPerTickRequirement> CODEC = NamedCodec.record(energyPerTickRequirementInstance ->
            energyPerTickRequirementInstance.group(
                    RequirementIOMode.CODEC.fieldOf("mode").forGetter(IRequirement::getMode),
                    NamedCodec.INT.fieldOf("amount").forGetter(requirement -> requirement.amount)
            ).apply(energyPerTickRequirementInstance, EnergyPerTickRequirement::new), "Energy per tick requirement"
    );

    @Override
    public RequirementType<EnergyPerTickRequirement> getType() {
        return Registration.ENERGY_PER_TICK_REQUIREMENT.get();
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
        int amount = (int)context.getPerTickIntegerModifiedValue(this.amount, this, null);
        if(getMode() == RequirementIOMode.INPUT)
            return energy.extractRecipeEnergy(amount, true) == amount;
        else
            return energy.receiveRecipeEnergy(amount, true) == amount;
    }

    @Override
    public void gatherRequirements(IRequirementList<EnergyMachineComponent> list) {
        if(this.mode == RequirementIOMode.INPUT)
            list.processEachTick(this::processInputs);
        else
            list.processEachTick(this::processOutputs);
    }

    private CraftingResult processInputs(EnergyMachineComponent component, ICraftingContext context) {
        int amount = (int)context.getPerTickIntegerModifiedValue(this.amount, this, null);
        int canExtract = component.extractRecipeEnergy(amount, true);
        if(canExtract == amount) {
            component.extractRecipeEnergy(amount, false);
            return CraftingResult.success();
        }
        return CraftingResult.error(Component.translatable("custommachinery.requirements.energypertick.error.input", amount, canExtract));
    }

    private CraftingResult processOutputs(EnergyMachineComponent component, ICraftingContext context) {
        int amount = (int)context.getPerTickIntegerModifiedValue(this.amount, this, null);
        int canReceive = component.receiveRecipeEnergy(amount, true);
        if(canReceive == amount) {
            component.receiveRecipeEnergy(amount, false);
            return CraftingResult.success();
        }
        return CraftingResult.error(Component.translatable("custommachinery.requirements.energypertick.error.output", amount));
    }

    @Override
    public List<IJEIIngredientWrapper<Energy>> getJEIIngredientWrappers(IMachineRecipe recipe, RecipeRequirement<?, ?> requirement) {
        return Collections.singletonList(new EnergyIngredientWrapper(this.getMode(), this.amount, requirement.chance(), true, recipe.getRecipeTime()));
    }
}

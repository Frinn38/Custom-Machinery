package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.ITickableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.client.integration.jei.wrapper.EnergyIngredientWrapper;
import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.integration.jei.Energy;
import fr.frinn.custommachinery.impl.requirement.AbstractChanceableRequirement;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Collections;
import java.util.List;

public class EnergyPerTickRequirement extends AbstractChanceableRequirement<EnergyMachineComponent> implements ITickableRequirement<EnergyMachineComponent>, IJEIIngredientRequirement<Energy> {

    public static final NamedCodec<EnergyPerTickRequirement> CODEC = NamedCodec.record(energyPerTickRequirementInstance ->
            energyPerTickRequirementInstance.group(
                    RequirementIOMode.CODEC.fieldOf("mode").forGetter(IRequirement::getMode),
                    NamedCodec.INT.fieldOf("amount").forGetter(requirement -> requirement.amount),
                    NamedCodec.doubleRange(0.0, 1.0).optionalFieldOf("chance", 1.0D).forGetter(AbstractChanceableRequirement::getChance)
            ).apply(energyPerTickRequirementInstance, (mode, amount, chance) -> {
                    EnergyPerTickRequirement requirement = new EnergyPerTickRequirement(mode, amount);
                    requirement.setChance(chance);
                    return requirement;
            }), "Energy per tick requirement"
    );

    private final int amount;

    public EnergyPerTickRequirement(RequirementIOMode mode, int amount) {
        super(mode);
        this.amount = amount;
    }

    @Override
    public RequirementType<EnergyPerTickRequirement> getType() {
        return Registration.ENERGY_PER_TICK_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType<EnergyMachineComponent> getComponentType() {
        return Registration.ENERGY_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean test(EnergyMachineComponent energy, ICraftingContext context) {
        int amount = (int)context.getPerTickModifiedValue(this.amount, this, null);
        if(getMode() == RequirementIOMode.INPUT)
            return energy.extractRecipeEnergy(amount, true) == amount;
        else
            return energy.receiveRecipeEnergy(amount, true) == amount;
    }

    @Override
    public CraftingResult processStart(EnergyMachineComponent energy, ICraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processTick(EnergyMachineComponent energy, ICraftingContext context) {
        int amount = (int)context.getPerTickModifiedValue(this.amount, this, null);
        if(getMode() == RequirementIOMode.INPUT) {
            int canExtract = energy.extractRecipeEnergy(amount, true);
            if(canExtract == amount) {
                energy.extractRecipeEnergy(amount, false);
                return CraftingResult.success();
            }
            return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.energypertick.error.input", amount, canExtract));
        }
        else {
            int canReceive = energy.receiveRecipeEnergy(amount, true);
            if(canReceive == amount) {
                energy.receiveRecipeEnergy(amount, false);
                return CraftingResult.success();
            }
            return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.energypertick.error.output", amount));
        }
    }

    @Override
    public CraftingResult processEnd(EnergyMachineComponent energy, ICraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public List<IJEIIngredientWrapper<Energy>> getJEIIngredientWrappers(IMachineRecipe recipe) {
        return Collections.singletonList(new EnergyIngredientWrapper(this.getMode(), this.amount, getChance(), true, recipe.getRecipeTime()));
    }
}

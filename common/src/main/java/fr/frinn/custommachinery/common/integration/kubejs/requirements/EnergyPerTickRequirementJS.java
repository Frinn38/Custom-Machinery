package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.EnergyPerTickRequirement;

public interface EnergyPerTickRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireEnergyPerTick(int amount) {
        return this.addRequirement(new EnergyPerTickRequirement(RequirementIOMode.INPUT, amount));
    }

    default RecipeJSBuilder produceEnergyPerTick(int amount) {
        return this.addRequirement(new EnergyPerTickRequirement(RequirementIOMode.OUTPUT, amount));
    }
}

package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.EnergyRequirement;

public interface EnergyRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireEnergy(int amount) {
        return this.addRequirement(new EnergyRequirement(RequirementIOMode.INPUT, amount));
    }

    default RecipeJSBuilder produceEnergy(int amount) {
        return this.addRequirement(new EnergyRequirement(RequirementIOMode.OUTPUT, amount));
    }
}

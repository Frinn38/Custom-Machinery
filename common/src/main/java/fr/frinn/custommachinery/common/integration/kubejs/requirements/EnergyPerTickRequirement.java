package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.integration.kubejs.RecipeJSBuilder;

public interface EnergyPerTickRequirement extends RecipeJSBuilder {

    default RecipeJSBuilder requireEnergyPerTick(int amount) {
        return this.addRequirement(new fr.frinn.custommachinery.common.requirement.EnergyPerTickRequirement(RequirementIOMode.INPUT, amount));
    }

    default RecipeJSBuilder produceEnergyPerTick(int amount) {
        return this.addRequirement(new fr.frinn.custommachinery.common.requirement.EnergyPerTickRequirement(RequirementIOMode.OUTPUT, amount));
    }
}

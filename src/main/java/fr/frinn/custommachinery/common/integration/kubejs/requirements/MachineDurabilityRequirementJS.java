package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.requirement.MachineDurabilityRequirement;

public interface MachineDurabilityRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder damageMachine() {
        return this.damageMachine(1);
    }

    default RecipeJSBuilder damageMachine(int amount) {
        return this.addRequirement(new MachineDurabilityRequirement(RequirementIOMode.INPUT, amount));
    }

    default RecipeJSBuilder repairMachine() {
        return this.repairMachine(1);
    }

    default RecipeJSBuilder repairMachine(int amount) {
        return this.addRequirement(new MachineDurabilityRequirement(RequirementIOMode.OUTPUT, amount));
    }
}

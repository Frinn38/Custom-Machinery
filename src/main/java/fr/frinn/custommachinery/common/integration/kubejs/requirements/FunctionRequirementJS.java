package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.FunctionRequirement;

public interface FunctionRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireFunctionToStart(String id) {
        return this.addRequirement(new FunctionRequirement(FunctionRequirement.Phase.CHECK, id));
    }

    default RecipeJSBuilder requireFunctionOnStart(String id) {
        return this.addRequirement(new FunctionRequirement(FunctionRequirement.Phase.START, id));
    }

    default RecipeJSBuilder requireFunctionEachTick(String id) {
        return this.addRequirement(new FunctionRequirement(FunctionRequirement.Phase.TICK, id));
    }

    default RecipeJSBuilder requireFunctionOnEnd(String id) {
        return this.addRequirement(new FunctionRequirement(FunctionRequirement.Phase.END, id));
    }
}

package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import dev.latvian.mods.kubejs.script.ScriptType;
import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.integration.kubejs.function.KJSFunction;
import fr.frinn.custommachinery.common.integration.kubejs.function.RecipeFunction;
import fr.frinn.custommachinery.common.requirement.FunctionRequirement;

public interface FunctionRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireFunctionToStart(RecipeFunction function) {
        return this.addRequirement(new FunctionRequirement(FunctionRequirement.Phase.CHECK, new KJSFunction(function), error -> ScriptType.SERVER.console.error(error.getLocalizedMessage())));
    }

    default RecipeJSBuilder requireFunctionOnStart(RecipeFunction function) {
        return this.addRequirement(new FunctionRequirement(FunctionRequirement.Phase.START, new KJSFunction(function), error -> ScriptType.SERVER.console.error(error.getLocalizedMessage())));
    }

    default RecipeJSBuilder requireFunctionEachTick(RecipeFunction function) {
        return this.addRequirement(new FunctionRequirement(FunctionRequirement.Phase.TICK, new KJSFunction(function), error -> ScriptType.SERVER.console.error(error.getLocalizedMessage())));
    }

    default RecipeJSBuilder requireFunctionOnEnd(RecipeFunction function) {
        return this.addRequirement(new FunctionRequirement(FunctionRequirement.Phase.END, new KJSFunction(function), error -> ScriptType.SERVER.console.error(error.getLocalizedMessage())));
    }
}

package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.CommandRequirement;

public interface CommandRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder runCommandOnStart(String command) {
        return this.runCommandOnStart(command, 2, false);
    }

    default RecipeJSBuilder runCommandOnStart(String command, int permissionLevel) {
        return this.runCommandOnStart(command, permissionLevel, false);
    }

    default RecipeJSBuilder runCommandOnStart(String command, boolean log) {
        return this.runCommandOnStart(command, 2, log);
    }

    default RecipeJSBuilder runCommandOnStart(String command, int permissionLevel, boolean log) {
        return this.addRequirement(new CommandRequirement(command, permissionLevel, log, false)).delay(0.0D);
    }

    default RecipeJSBuilder runCommandEachTick(String command) {
        return this.runCommandEachTick(command, 2, false);
    }

    default RecipeJSBuilder runCommandEachTick(String command, int permissionLevel) {
        return this.runCommandEachTick(command, permissionLevel, false);
    }

    default RecipeJSBuilder runCommandEachTick(String command, boolean log) {
        return this.runCommandEachTick(command, 2, log);
    }

    default RecipeJSBuilder runCommandEachTick(String command, int permissionLevel, boolean log) {
        return this.addRequirement(new CommandRequirement(command, permissionLevel, log, true));
    }

    default RecipeJSBuilder runCommandOnEnd(String command) {
        return this.runCommandOnEnd(command, 2, false);
    }

    default RecipeJSBuilder runCommandOnEnd(String command, int permissionLevel) {
        return this.runCommandOnEnd(command, permissionLevel, false);
    }

    default RecipeJSBuilder runCommandOnEnd(String command, boolean log) {
        return this.runCommandOnEnd(command, 2, log);
    }

    default RecipeJSBuilder runCommandOnEnd(String command, int permissionLevel, boolean log) {
        return this.addRequirement(new CommandRequirement(command, permissionLevel, log, false)).delay(1.0D);
    }
}

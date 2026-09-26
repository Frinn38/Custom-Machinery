package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.WorkingCoreRequirement;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public interface WorkingCoreRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireWorkingCore() {
        return this.requireWorkingCore(0, null);
    }

    default RecipeJSBuilder requireWorkingCore(Object obj) {
        return switch (obj) {
            case Number number -> this.requireWorkingCore(number.intValue(), null);
            case Identifier recipe -> this.requireWorkingCore(0, recipe);
            case CharSequence string when Identifier.tryParse(string.toString()) != null -> this.requireWorkingCore(0, Identifier.tryParse(string.toString()));
            default -> this.error("Invalid argument {} in 'requireWorkingCore' method\nMust be either core id or recipe id !", obj);
        };
    }

    default RecipeJSBuilder requireWorkingCore(int core, @Nullable Identifier recipe) {
        return this.addRequirement(new WorkingCoreRequirement(core, recipe));
    }
}

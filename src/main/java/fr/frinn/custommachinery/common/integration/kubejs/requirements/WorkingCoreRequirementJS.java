package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.WorkingCoreRequirement;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface WorkingCoreRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireWorkingCore() {
        return this.requireWorkingCore(0, null);
    }

    default RecipeJSBuilder requireWorkingCore(Object obj) {
        if(obj instanceof Number number)
            return this.requireWorkingCore(number.intValue(), null);
        else if(obj instanceof ResourceLocation recipe)
            return this.requireWorkingCore(0, recipe);
        else if(obj instanceof CharSequence string && ResourceLocation.tryParse(string.toString()) != null)
            return this.requireWorkingCore(0, ResourceLocation.tryParse(string.toString()));
        else
            return this.error("Invalid argument {} in 'requireWorkingCore' method\nMust be either core id or recipe id !", obj);
    }

    default RecipeJSBuilder requireWorkingCore(int core, @Nullable ResourceLocation recipe) {
        return this.addRequirement(new WorkingCoreRequirement(core, recipe));
    }
}

package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.requirement.DurabilityRequirement;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Collections;
import java.util.List;

public interface DurabilityRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder damageItem(Ingredient ingredient, int amount) {
        return this.damageItem(ingredient, amount, Collections.emptyList());
    }

    default RecipeJSBuilder damageItem(Ingredient ingredient, int amount, List<String> slots) {
        return this.addRequirement(new DurabilityRequirement(RequirementIOMode.INPUT, ingredient, amount, true, slots));
    }

    default RecipeJSBuilder damageItemNoBreak(Ingredient ingredient, int amount) {
        return this.damageItem(ingredient, amount,Collections.emptyList());
    }

    default RecipeJSBuilder damageItemNoBreak(Ingredient ingredient, int amount, List<String> slots) {
        return this.addRequirement(new DurabilityRequirement(RequirementIOMode.INPUT, ingredient, amount, false, slots));
    }

    default RecipeJSBuilder repairItem(Ingredient ingredient, int amount) {
        return this.repairItem(ingredient, amount, Collections.emptyList());
    }

    default RecipeJSBuilder repairItem(Ingredient ingredient, int amount, List<String> slots) {
        return this.addRequirement(new DurabilityRequirement(RequirementIOMode.OUTPUT, ingredient, amount, false, slots));
    }
}

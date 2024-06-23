package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.requirement.DurabilityRequirement;
import net.minecraft.world.item.crafting.Ingredient;

public interface DurabilityRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder damageItem(Ingredient ingredient, int amount) {
        return this.damageItem(ingredient, amount,"");
    }

    default RecipeJSBuilder damageItem(Ingredient ingredient, int amount, String slot) {
        return this.addRequirement(new DurabilityRequirement(RequirementIOMode.INPUT, ingredient, amount, true, slot));
    }

    default RecipeJSBuilder damageItemNoBreak(Ingredient ingredient, int amount) {
        return this.damageItem(ingredient, amount,"");
    }

    default RecipeJSBuilder damageItemNoBreak(Ingredient ingredient, int amount, String slot) {
        return this.addRequirement(new DurabilityRequirement(RequirementIOMode.INPUT, ingredient, amount, false, slot));
    }

    default RecipeJSBuilder repairItem(Ingredient ingredient, int amount) {
        return this.repairItem(ingredient, amount, "");
    }

    default RecipeJSBuilder repairItem(Ingredient ingredient, int amount, String slot) {
        return this.addRequirement(new DurabilityRequirement(RequirementIOMode.OUTPUT, ingredient, amount, false, slot));
    }
}

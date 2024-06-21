package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.ItemFilterRequirement;
import net.minecraft.world.item.crafting.Ingredient;

public interface ItemFilterRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireItemFilter(Ingredient ingredient) {
        return requireItemFilter(ingredient, "");
    }

    default RecipeJSBuilder requireItemFilter(Ingredient ingredient, String slot) {
        return this.addRequirement(new ItemFilterRequirement(ingredient, slot));
    }
}

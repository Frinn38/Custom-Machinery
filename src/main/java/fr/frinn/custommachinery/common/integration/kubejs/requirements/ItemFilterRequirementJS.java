package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.ItemFilterRequirement;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Collections;
import java.util.List;

public interface ItemFilterRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireItemFilter(Ingredient ingredient) {
        return requireItemFilter(ingredient, Collections.emptyList());
    }

    default RecipeJSBuilder requireItemFilter(Ingredient ingredient, List<String> slots) {
        return this.addRequirement(new ItemFilterRequirement(ingredient, slots));
    }
}

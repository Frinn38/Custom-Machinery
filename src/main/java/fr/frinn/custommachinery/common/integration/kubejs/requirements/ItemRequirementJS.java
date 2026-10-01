package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.requirement.ItemEmptyRequirement;
import fr.frinn.custommachinery.common.requirement.ItemRequirement;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.Collections;
import java.util.List;

public interface ItemRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireItem(SizedIngredient ingredient) {
        return this.requireItem(ingredient, Collections.emptyList());
    }

    default RecipeJSBuilder requireItem(SizedIngredient ingredient, List<String> slots) {
        if(ingredient.getItems().length == 0)
            return this.error("Invalid empty ingredient in item input requirement");
        return this.addRequirement(new ItemRequirement(RequirementIOMode.INPUT, ingredient, slots, false));
    }

    default RecipeJSBuilder requireItemOnEnd(SizedIngredient ingredient) {
        return this.requireItemOnEnd(ingredient, Collections.emptyList());
    }

    default RecipeJSBuilder requireItemOnEnd(SizedIngredient ingredient, List<String> slots) {
        if(ingredient.getItems().length == 0)
            return this.error("Invalid empty ingredient in item input requirement");
        return this.addRequirement(new ItemRequirement(RequirementIOMode.INPUT, ingredient, slots, true));
    }

    default RecipeJSBuilder requireEmptyItem() {
        return this.requireEmptyItem("");
    }

    default RecipeJSBuilder requireEmptyItem(String slot) {
        return this.addRequirement(new ItemEmptyRequirement(slot));
    }

    default RecipeJSBuilder produceItem(ItemStack stack) {
        return this.produceItem(stack, Collections.emptyList());
    }

    default RecipeJSBuilder produceItem(ItemStack stack, List<String> slots) {
        if(stack.isEmpty())
            return this.error("Invalid empty item in item output requirement");
        Ingredient ingredient = stack.isComponentsPatchEmpty() ? Ingredient.of(stack) : DataComponentIngredient.of(true, stack);
        return this.addRequirement(new ItemRequirement(RequirementIOMode.OUTPUT, new SizedIngredient(ingredient, stack.getCount()), slots, false));
    }
}

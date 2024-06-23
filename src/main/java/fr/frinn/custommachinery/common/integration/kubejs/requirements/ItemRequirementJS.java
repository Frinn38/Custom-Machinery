package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.requirement.ItemRequirement;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public interface ItemRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireItem(ItemStack stack) {
        if(stack.isEmpty())
            return error("Can't require empty item, if you want to require a tag use '.requireIngredient(tag)' instead");
        return this.requireItem(stack, "");
    }

    default RecipeJSBuilder requireItem(ItemStack stack, String slot) {
        if(stack.isEmpty())
            return error("Can't require empty item, if you want to require a tag use '.requireIngredient(tag)' instead");
        return this.addRequirement(new ItemRequirement(RequirementIOMode.INPUT, Ingredient.of(stack), stack.getCount(), slot));
    }

    default RecipeJSBuilder requireIngredient(Ingredient ingredient) {
        return this.requireIngredient(ingredient, 1, "");
    }

    default RecipeJSBuilder requireIngredient(Ingredient ingredient, int amount) {
        return this.requireIngredient(ingredient, amount, "");
    }

    default RecipeJSBuilder requireIngredient(Ingredient ingredient, int amount, String slot) {
        return this.addRequirement(new ItemRequirement(RequirementIOMode.INPUT, ingredient, amount, slot));
    }

    default RecipeJSBuilder produceItem(ItemStack stack) {
        return this.produceItem(stack, "");
    }

    default RecipeJSBuilder produceItem(ItemStack stack, String slot) {
        return this.addRequirement(new ItemRequirement(RequirementIOMode.OUTPUT, Ingredient.of(stack), stack.getCount(), slot));
    }
}

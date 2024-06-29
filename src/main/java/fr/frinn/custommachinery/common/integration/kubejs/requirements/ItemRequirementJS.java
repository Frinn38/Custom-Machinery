package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.requirement.ItemRequirement;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public interface ItemRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireItem(SizedIngredient ingredient) {
        return this.requireItem(ingredient, "");
    }

    default RecipeJSBuilder requireItem(SizedIngredient ingredient, String slot) {
        if(ingredient.getItems().length == 0)
            return this.error("Invalid empty ingredient in item input requirement");
        return this.addRequirement(new ItemRequirement(RequirementIOMode.INPUT, ingredient, slot));
    }

    default RecipeJSBuilder produceItem(ItemStack stack) {
        return this.produceItem(stack, "");
    }

    default RecipeJSBuilder produceItem(ItemStack stack, String slot) {
        if(stack.isEmpty())
            return this.error("Invalid empty item in item output requirement");
        return this.addRequirement(new ItemRequirement(RequirementIOMode.OUTPUT, new SizedIngredient(Ingredient.of(stack), stack.getCount()), slot));
    }
}

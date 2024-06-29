package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.requirement.DropRequirement;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public interface DropRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder checkDrop(ItemStack item, int amount, int radius) {
        return checkDrops(new SizedIngredient(Ingredient.of(item), amount), radius, true);
    }

    default RecipeJSBuilder checkAnyDrop(int amount, int radius) {
        return checkDrops(SizedIngredient.of(Items.AIR, amount), radius, false);
    }

    default RecipeJSBuilder checkDrops(SizedIngredient ingredient, int radius) {
        return checkDrops(ingredient, radius, true);
    }

    default RecipeJSBuilder checkDrops(SizedIngredient ingredient, int radius, boolean whitelist) {
        return addRequirement(new DropRequirement(RequirementIOMode.INPUT, DropRequirement.Action.CHECK, ingredient.ingredient(), whitelist, Items.AIR, ingredient.count(), radius));
    }

    default RecipeJSBuilder consumeDropOnStart(ItemStack item, int amount, int radius) {
        return consumeDropsOnStart(new SizedIngredient(Ingredient.of(item), amount), radius, true);
    }

    default RecipeJSBuilder consumeAnyDropOnStart(int amount, int radius) {
        return consumeDropsOnStart(SizedIngredient.of(Items.AIR, amount), radius, false);
    }

    default RecipeJSBuilder consumeDropsOnStart(SizedIngredient ingredient, int radius) {
        return consumeDropsOnStart(ingredient, radius, true);
    }

    default RecipeJSBuilder consumeDropsOnStart(SizedIngredient ingredient, int radius, boolean whitelist) {
        return addRequirement(new DropRequirement(RequirementIOMode.INPUT, DropRequirement.Action.CONSUME, ingredient.ingredient(), whitelist, Items.AIR, ingredient.count(), radius));
    }

    default RecipeJSBuilder consumeDropOnEnd(ItemStack item, int amount, int radius) {
        return consumeDropsOnEnd(new SizedIngredient(Ingredient.of(item), amount), radius, true);
    }

    default RecipeJSBuilder consumeAnyDropOnEnd(int amount, int radius) {
        return consumeDropsOnEnd(SizedIngredient.of(Items.AIR, amount), radius, false);
    }

    default RecipeJSBuilder consumeDropsOnEnd(SizedIngredient ingredient, int radius) {
        return consumeDropsOnEnd(ingredient, radius, true);
    }

    default RecipeJSBuilder consumeDropsOnEnd(SizedIngredient ingredient, int radius, boolean whitelist) {
        return addRequirement(new DropRequirement(RequirementIOMode.OUTPUT, DropRequirement.Action.CONSUME, ingredient.ingredient(), whitelist, Items.AIR, ingredient.count(), radius));
    }

    default RecipeJSBuilder dropItemOnStart(ItemStack stack) {
        return addRequirement(new DropRequirement(RequirementIOMode.INPUT, DropRequirement.Action.PRODUCE, Ingredient.EMPTY, true, stack.getItem(), stack.getCount(), 1));
    }

    default RecipeJSBuilder dropItemOnEnd(ItemStack stack) {
        return addRequirement(new DropRequirement(RequirementIOMode.OUTPUT, DropRequirement.Action.PRODUCE, Ingredient.EMPTY, true, stack.getItem(), stack.getCount(), 1));
    }
}

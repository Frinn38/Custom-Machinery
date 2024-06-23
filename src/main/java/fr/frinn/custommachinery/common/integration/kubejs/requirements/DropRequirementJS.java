package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.requirement.DropRequirement;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public interface DropRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder checkDrop(ItemStack item, int amount, int radius) {
        return checkDrops(Ingredient.of(item), amount, radius, true);
    }

    default RecipeJSBuilder checkAnyDrop(int amount, int radius) {
        return checkDrops(Ingredient.EMPTY, amount, radius, false);
    }

    default RecipeJSBuilder checkDrops(Ingredient ingredient, int amount, int radius) {
        return checkDrops(ingredient, amount, radius, true);
    }

    default RecipeJSBuilder checkDrops(Ingredient ingredient, int amount, int radius, boolean whitelist) {
        return addRequirement(new DropRequirement(RequirementIOMode.INPUT, DropRequirement.Action.CHECK, ingredient, whitelist, Items.AIR, amount, radius));
    }

    default RecipeJSBuilder consumeDropOnStart(ItemStack item, int amount, int radius) {
        return consumeDropsOnStart(Ingredient.of(item), amount, radius, true);
    }

    default RecipeJSBuilder consumeAnyDropOnStart(int amount, int radius) {
        return consumeDropsOnStart(Ingredient.EMPTY, amount, radius, false);
    }

    default RecipeJSBuilder consumeDropsOnStart(Ingredient ingredient, int amount, int radius) {
        return consumeDropsOnStart(ingredient, amount, radius, true);
    }

    default RecipeJSBuilder consumeDropsOnStart(Ingredient ingredient, int amount, int radius, boolean whitelist) {
        return addRequirement(new DropRequirement(RequirementIOMode.INPUT, DropRequirement.Action.CONSUME, ingredient, whitelist, Items.AIR, amount, radius));
    }

    default RecipeJSBuilder consumeDropOnEnd(ItemStack item, int amount, int radius) {
        return consumeDropsOnEnd(Ingredient.of(item), amount, radius, true);
    }

    default RecipeJSBuilder consumeAnyDropOnEnd(int amount, int radius) {
        return consumeDropsOnEnd(Ingredient.EMPTY, amount, radius, false);
    }

    default RecipeJSBuilder consumeDropsOnEnd(Ingredient ingredient, int amount, int radius) {
        return consumeDropsOnEnd(ingredient, amount, radius, true);
    }

    default RecipeJSBuilder consumeDropsOnEnd(Ingredient ingredient, int amount, int radius, boolean whitelist) {
        return addRequirement(new DropRequirement(RequirementIOMode.OUTPUT, DropRequirement.Action.CONSUME, ingredient, whitelist, Items.AIR, amount, radius));
    }

    default RecipeJSBuilder dropItemOnStart(ItemStack stack) {
        return addRequirement(new DropRequirement(RequirementIOMode.INPUT, DropRequirement.Action.PRODUCE, Ingredient.EMPTY, true, stack.getItem(), stack.getCount(), 1));
    }

    default RecipeJSBuilder dropItemOnEnd(ItemStack stack) {
        return addRequirement(new DropRequirement(RequirementIOMode.OUTPUT, DropRequirement.Action.PRODUCE, Ingredient.EMPTY, true, stack.getItem(), stack.getCount(), 1));
    }
}

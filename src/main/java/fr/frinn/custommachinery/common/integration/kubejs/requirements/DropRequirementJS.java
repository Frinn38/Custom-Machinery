package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.requirement.DropRequirement;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import fr.frinn.custommachinery.common.util.ingredient.ItemIngredient;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static fr.frinn.custommachinery.common.integration.kubejs.KubeJSIntegration.nbtFromStack;

public interface DropRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder checkDrop(ItemStack item, int amount, int radius) {
        return checkDrops(new ItemStack[]{item}, amount, radius, true);
    }

    default RecipeJSBuilder checkAnyDrop(int amount, int radius) {
        return checkDrops(new ItemStack[]{}, amount, radius, false);
    }

    default RecipeJSBuilder checkDrops(ItemStack[] items, int amount, int radius) {
        return checkDrops(items, amount, radius, true);
    }

    default RecipeJSBuilder checkDrops(ItemStack[] items, int amount, int radius, boolean whitelist) {
        if(items.length == 0)
            return error("Invalid Drop requirement, checkDrop method must have at least 1 item defined when using whitelist mode");

        List<IIngredient<Item>> input = Arrays.stream(items).map(ItemStack::getItem).map(ItemIngredient::new).collect(Collectors.toList());
        return addRequirement(new DropRequirement(RequirementIOMode.INPUT, DropRequirement.Action.CHECK, input, whitelist, Items.AIR, nbtFromStack(items[0]), amount, radius));
    }

    default RecipeJSBuilder consumeDropOnStart(ItemStack item, int amount, int radius) {
        return consumeDropsOnStart(new ItemStack[]{item}, amount, radius, true);
    }

    default RecipeJSBuilder consumeAnyDropOnStart(int amount, int radius) {
        return consumeDropsOnStart(new ItemStack[]{}, amount, radius, false);
    }

    default RecipeJSBuilder consumeDropsOnStart(ItemStack[] items, int amount, int radius) {
        return consumeDropsOnStart(items, amount, radius, true);
    }

    default RecipeJSBuilder consumeDropsOnStart(ItemStack[] items, int amount, int radius, boolean whitelist) {
        if(items.length == 0)
            return error("Invalid Drop requirement, consumeDropOnStart method must have at least 1 item defined when using whitelist mode");

        List<IIngredient<Item>> input = Arrays.stream(items).map(ItemStack::getItem).map(ItemIngredient::new).collect(Collectors.toList());
        return addRequirement(new DropRequirement(RequirementIOMode.INPUT, DropRequirement.Action.CONSUME, input, whitelist, Items.AIR, nbtFromStack(items[0]), amount, radius));
    }

    default RecipeJSBuilder consumeDropOnEnd(ItemStack item, int amount, int radius) {
        return consumeDropsOnEnd(new ItemStack[]{item}, amount, radius, true);
    }

    default RecipeJSBuilder consumeAnyDropOnEnd(int amount, int radius) {
        return consumeDropsOnEnd(new ItemStack[]{}, amount, radius, false);
    }

    default RecipeJSBuilder consumeDropsOnEnd(ItemStack[] items, int amount, int radius) {
        return consumeDropsOnEnd(items, amount, radius, true);
    }

    default RecipeJSBuilder consumeDropsOnEnd(ItemStack[] items, int amount, int radius, boolean whitelist) {
        if(items.length == 0)
            return error("Invalid Drop requirement, consumeDropOnEnd method must have at least 1 item defined when using whitelist mode");

        List<IIngredient<Item>> input = Arrays.stream(items).map(ItemStack::getItem).map(ItemIngredient::new).collect(Collectors.toList());
        return addRequirement(new DropRequirement(RequirementIOMode.OUTPUT, DropRequirement.Action.CONSUME, input, whitelist, Items.AIR, nbtFromStack(items[0]), amount, radius));
    }

    default RecipeJSBuilder dropItemOnStart(ItemStack stack) {
        return addRequirement(new DropRequirement(RequirementIOMode.INPUT, DropRequirement.Action.PRODUCE, Collections.emptyList(), true, stack.getItem(), nbtFromStack(stack), stack.getCount(), 1));
    }

    default RecipeJSBuilder dropItemOnEnd(ItemStack stack) {
        return addRequirement(new DropRequirement(RequirementIOMode.OUTPUT, DropRequirement.Action.PRODUCE, Collections.emptyList(), true, stack.getItem(), nbtFromStack(stack), stack.getCount(), 1));
    }
}

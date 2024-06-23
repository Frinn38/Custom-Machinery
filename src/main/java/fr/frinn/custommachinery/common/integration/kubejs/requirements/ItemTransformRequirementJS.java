package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.ItemTransformRequirement;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Function;

public interface ItemTransformRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder transformItem(ItemStack input) {
        return transformItem(input, input);
    }

    default RecipeJSBuilder transformItem(ItemStack input, ItemStack output) {
        return transformItem(input, output, "", "");
    }

    default RecipeJSBuilder transformItem(ItemStack input, ItemStack output, String inputSlot, String outputSlot) {
        return transformItem(input, output, inputSlot, outputSlot, null);
    }

    default RecipeJSBuilder transformItem(ItemStack input, ItemStack output, String inputSlot, String outputSlot, Function<ItemStack, ItemStack> function) {
        return this.addRequirement(new ItemTransformRequirement(Ingredient.of(input), input.getCount(), inputSlot, output.getItem(), output.getCount(), outputSlot, true, function));
    }
}

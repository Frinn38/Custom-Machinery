package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.ItemTransformRequirement;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface ItemTransformRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder transformItem(SizedIngredient input, ItemStack output) {
        return transformItem(input, output, "", "");
    }

    default RecipeJSBuilder transformItem(SizedIngredient input, ItemStack output, String inputSlot, String outputSlot) {
        return transformItem(input, output, inputSlot, outputSlot, null);
    }

    default RecipeJSBuilder transformItem(SizedIngredient input, ItemStack output, String inputSlot, String outputSlot, @Nullable Function<ItemStack, ItemStack> function) {
        return this.addRequirement(new ItemTransformRequirement(input.ingredient(), input.count(), inputSlot, output, output.getCount(), outputSlot, true, function));
    }
}

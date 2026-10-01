package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.ItemTransformRequirement;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public interface ItemTransformRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder transformItem(SizedIngredient input, ItemStack output) {
        return transformItem(input, output, Collections.emptyList(), Collections.emptyList());
    }

    default RecipeJSBuilder transformItem(SizedIngredient input, ItemStack output, List<String> inputSlots, List<String> outputSlots) {
        return transformItem(input, output, inputSlots, outputSlots, null);
    }

    default RecipeJSBuilder transformItem(SizedIngredient input, ItemStack output, List<String> inputSlots, List<String> outputSlots, @Nullable Function<ItemStack, ItemStack> function) {
        return this.addRequirement(new ItemTransformRequirement(input.ingredient(), input.count(), inputSlots, output, output.getCount(), outputSlots, true, function));
    }
}

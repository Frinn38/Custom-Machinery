package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import dev.latvian.mods.kubejs.util.MapJS;
import dev.latvian.mods.rhino.mod.util.NBTUtils;
import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.ItemTransformRequirement;
import fr.frinn.custommachinery.common.util.ingredient.ItemIngredient;
import fr.frinn.custommachinery.common.util.ingredient.ItemTagIngredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
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

    default RecipeJSBuilder transformItem(ItemStack input, ItemStack output, String inputSlot, String outputSlot, Function<Map<?,?>, Object> nbt) {
        return this.addRequirement(new ItemTransformRequirement(new ItemIngredient(input.getItem()), input.getCount(), inputSlot, input.getTag(), output.getItem(), output.getCount(), outputSlot, true, new NbtTransformer(nbt)));
    }

    default RecipeJSBuilder transformItemTag(String tag) {
        return transformItemTag(tag, 1, null);
    }

    default RecipeJSBuilder transformItemTag(String tag, int inputAmount, CompoundTag inputNBT) {
        return transformItemTag(tag, inputAmount, inputNBT, ItemStack.EMPTY);
    }

    default RecipeJSBuilder transformItemTag(String tag, int inputAmount, CompoundTag inputNBT, ItemStack output) {
        return transformItemTag(tag, inputAmount, inputNBT, output, "", "");
    }

    default RecipeJSBuilder transformItemTag(String tag, int inputAmount, CompoundTag inputNBT, ItemStack output, String inputSlot, String outputSlot) {
        return transformItemTag(tag, inputAmount, inputNBT, output, inputSlot, outputSlot, null);
    }

    default RecipeJSBuilder transformItemTag(String tag, int inputAmount, CompoundTag inputNBT, ItemStack output, String inputSlot, String outputSlot, Function<Map<?,?>, Object> nbt) {
        try {
            return this.addRequirement(new ItemTransformRequirement(ItemTagIngredient.create(tag), inputAmount, inputSlot, inputNBT, output.getItem(), output.getCount(), outputSlot, true, new NbtTransformer(nbt)));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    record NbtTransformer(Function<Map<?,?>, Object> function) implements Function<CompoundTag, CompoundTag> {

        @Nullable
        @Override
        public CompoundTag apply(@Nullable CompoundTag compoundTag) {
            Map<?,?> map = MapJS.of(this.function.apply(MapJS.of(compoundTag)));
            return map == null ? null : NBTUtils.toTagCompound(map);
        }
    }
}

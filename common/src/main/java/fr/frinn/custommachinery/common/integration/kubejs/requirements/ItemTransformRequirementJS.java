package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.util.MapJS;
import fr.frinn.custommachinery.common.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.ItemTransformRequirement;
import fr.frinn.custommachinery.common.util.ingredient.ItemIngredient;
import fr.frinn.custommachinery.common.util.ingredient.ItemTagIngredient;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface ItemTransformRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder transformItem(ItemStackJS input) {
        return transformItem(input, input);
    }

    default RecipeJSBuilder transformItem(ItemStackJS input, ItemStackJS output) {
        return transformItem(input, output, "", "");
    }

    default RecipeJSBuilder transformItem(ItemStackJS input, ItemStackJS output, String inputSlot, String outputSlot) {
        return transformItem(input, output, inputSlot, outputSlot, null);
    }

    default RecipeJSBuilder transformItem(ItemStackJS input, ItemStackJS output, String inputSlot, String outputSlot, Function<MapJS, Object> nbt) {
        return this.addRequirement(new ItemTransformRequirement(new ItemIngredient(input.getItem()), input.getCount(), inputSlot, input.getNbt(), output.getItem(), output.getCount(), outputSlot, true, new NbtTransformer(nbt)));
    }

    default RecipeJSBuilder transformItemTag(String tag) {
        return transformItemTag(tag, 1, null);
    }

    default RecipeJSBuilder transformItemTag(String tag, int inputAmount, CompoundTag inputNBT) {
        return transformItemTag(tag, inputAmount, inputNBT, ItemStackJS.EMPTY);
    }

    default RecipeJSBuilder transformItemTag(String tag, int inputAmount, CompoundTag inputNBT, ItemStackJS output) {
        return transformItemTag(tag, inputAmount, inputNBT, output, "", "");
    }

    default RecipeJSBuilder transformItemTag(String tag, int inputAmount, CompoundTag inputNBT, ItemStackJS output, String inputSlot, String outputSlot) {
        return transformItemTag(tag, inputAmount, inputNBT, output, inputSlot, outputSlot, null);
    }

    default RecipeJSBuilder transformItemTag(String tag, int inputAmount, CompoundTag inputNBT, ItemStackJS output, String inputSlot, String outputSlot, Function<MapJS, Object> nbt) {
        try {
            return this.addRequirement(new ItemTransformRequirement(ItemTagIngredient.create(tag), inputAmount, inputSlot, inputNBT, output.getItem(), output.getCount(), outputSlot, true, new NbtTransformer(nbt)));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    record NbtTransformer(Function<MapJS, Object> function) implements Function<CompoundTag, CompoundTag> {

        @Nullable
        @Override
        public CompoundTag apply(@Nullable CompoundTag compoundTag) {
            MapJS map = MapJS.of(this.function.apply(MapJS.of(compoundTag)));
            return map == null ? null : map.toNBT();
        }
    }
}

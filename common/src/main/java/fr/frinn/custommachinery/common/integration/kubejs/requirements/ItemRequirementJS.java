package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import dev.latvian.mods.kubejs.util.MapJS;
import dev.latvian.mods.rhino.mod.util.NBTUtils;
import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.integration.kubejs.KubeJSIntegration;
import fr.frinn.custommachinery.common.requirement.ItemRequirement;
import fr.frinn.custommachinery.common.util.ingredient.ItemIngredient;
import fr.frinn.custommachinery.common.util.ingredient.ItemTagIngredient;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public interface ItemRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireItem(ItemStack stack) {
        return this.requireItem(stack, "");
    }

    default RecipeJSBuilder requireItem(ItemStack stack, String slot) {
        return this.addRequirement(new ItemRequirement(RequirementIOMode.INPUT, new ItemIngredient(stack.getItem()), stack.getCount(), KubeJSIntegration.nbtFromStack(stack), slot));
    }

    default RecipeJSBuilder requireItemTag(String tag, int amount) {
        return this.requireItemTag(tag, amount, null, "");
    }

    default RecipeJSBuilder requireItemTag(String tag, int amount, Object thing) {
        if(thing instanceof String)
            return this.requireItemTag(tag, amount, null, (String)thing);
        else
            return this.requireItemTag(tag, amount, MapJS.of(thing), "");
    }

    default RecipeJSBuilder requireItemTag(String tag, int amount, Map<?,?> nbt, String slot) {
        try {
            return this.addRequirement(new ItemRequirement(RequirementIOMode.INPUT, ItemTagIngredient.create(tag), amount, nbt == null ? null : NBTUtils.toTagCompound(nbt), slot));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    default RecipeJSBuilder produceItem(ItemStack stack) {
        return this.produceItem(stack, "");
    }

    default RecipeJSBuilder produceItem(ItemStack stack, String slot) {
        return this.addRequirement(new ItemRequirement(RequirementIOMode.OUTPUT, new ItemIngredient(stack.getItem()), stack.getCount(), KubeJSIntegration.nbtFromStack(stack), slot));
    }
}

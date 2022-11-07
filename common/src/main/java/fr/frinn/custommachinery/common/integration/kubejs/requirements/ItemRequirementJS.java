package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.util.MapJS;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.integration.kubejs.KubeJSIntegration;
import fr.frinn.custommachinery.common.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.ItemRequirement;
import fr.frinn.custommachinery.common.util.ingredient.ItemIngredient;
import fr.frinn.custommachinery.common.util.ingredient.ItemTagIngredient;

public interface ItemRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireItem(ItemStackJS stack) {
        return this.requireItem(stack, "");
    }

    default RecipeJSBuilder requireItem(ItemStackJS stack, String slot) {
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

    default RecipeJSBuilder requireItemTag(String tag, int amount, MapJS nbt, String slot) {
        try {
            return this.addRequirement(new ItemRequirement(RequirementIOMode.INPUT, ItemTagIngredient.create(tag), amount, nbt == null ? null : nbt.toNBT(), slot));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    default RecipeJSBuilder produceItem(ItemStackJS stack) {
        return this.produceItem(stack, "");
    }

    default RecipeJSBuilder produceItem(ItemStackJS stack, String slot) {
        return this.addRequirement(new ItemRequirement(RequirementIOMode.OUTPUT, new ItemIngredient(stack.getItem()), stack.getCount(), KubeJSIntegration.nbtFromStack(stack), slot));
    }
}

package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import fr.frinn.custommachinery.api.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.common.requirement.ItemRequirement;
import net.minecraft.world.item.crafting.Ingredient;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.OptionalInt;
import org.openzen.zencode.java.ZenCodeType.OptionalString;

@ZenRegister
@Name(CTConstants.REQUIREMENT_ITEM)
public interface ItemRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T requireItem(IItemStack stack, @OptionalString String slot) {
        return addRequirement(new ItemRequirement(RequirementIOMode.INPUT, Ingredient.of(stack.getInternal()), stack.amount(), slot));
    }

    @Method
    default T requireItemTag(IIngredient ingredient, @OptionalInt(1) int amount, @OptionalString String slot) {
        try {
            return addRequirement(new ItemRequirement(RequirementIOMode.INPUT, ingredient.asVanillaIngredient(), amount, slot));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    @Method
    default T produceItem(IItemStack stack, @OptionalString String slot) {
        return addRequirement(new ItemRequirement(RequirementIOMode.OUTPUT, Ingredient.of(stack.getInternal()), stack.amount(), slot));
    }
}
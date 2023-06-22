package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.IData;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.tag.MCTag;
import fr.frinn.custommachinery.api.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTUtils;
import fr.frinn.custommachinery.common.requirement.DurabilityRequirement;
import fr.frinn.custommachinery.common.util.ingredient.ItemIngredient;
import fr.frinn.custommachinery.common.util.ingredient.ItemTagIngredient;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.Optional;
import org.openzen.zencode.java.ZenCodeType.OptionalString;

@ZenRegister
@Name(CTConstants.REQUIREMENT_DURABILITY)
public interface DurabilityRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T damageItem(IItemStack stack, int amount, @OptionalString String slot) {
        return addRequirement(new DurabilityRequirement(RequirementIOMode.INPUT, new ItemIngredient(stack.getDefinition()), amount, CTUtils.nbtFromStack(stack), true, slot));
    }

    @Method
    default T damageItemNoBreak(IItemStack stack, int amount, @OptionalString String slot) {
        return addRequirement(new DurabilityRequirement(RequirementIOMode.INPUT, new ItemIngredient(stack.getDefinition()), amount, CTUtils.nbtFromStack(stack), false, slot));
    }

    @Method
    default T damageItemTag(MCTag tag, int amount, @Optional IData data, @OptionalString String slot) {
        try {
            return addRequirement(new DurabilityRequirement(RequirementIOMode.INPUT, ItemTagIngredient.create(tag.getTagKey()), amount, CTUtils.getNBT(data), true, slot));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    @Method
    default T damageItemTagNoBreak(MCTag tag, int amount, @Optional IData data, @OptionalString String slot) {
        try {
            return addRequirement(new DurabilityRequirement(RequirementIOMode.INPUT, ItemTagIngredient.create(tag.getTagKey()), amount, CTUtils.getNBT(data), false, slot));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    @Method
    default T repairItem(IItemStack stack, int amount, @OptionalString String slot) {
        return addRequirement(new DurabilityRequirement(RequirementIOMode.OUTPUT, new ItemIngredient(stack.getDefinition()), amount, CTUtils.nbtFromStack(stack), false, slot));
    }

    @Method
    default T repairItemTag(MCTag tag, int amount, @Optional IData data, @OptionalString String slot) {
        try {
            return addRequirement(new DurabilityRequirement(RequirementIOMode.OUTPUT, ItemTagIngredient.create(tag.getTagKey()), amount, CTUtils.getNBT(data), false, slot));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }
}

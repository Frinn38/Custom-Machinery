package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import fr.frinn.custommachinery.api.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.common.requirement.ItemFilterRequirement;
import org.openzen.zencode.java.ZenCodeType.OptionalString;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name(CTConstants.REQUIREMENT_ITEM_FILTER)
public interface ItemFilterRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T requireItemFilter(IIngredient stack, @OptionalString String slot) {
        return addRequirement(new ItemFilterRequirement(stack.asVanillaIngredient(), slot));
    }
}

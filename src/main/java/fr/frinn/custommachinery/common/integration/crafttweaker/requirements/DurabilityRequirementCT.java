package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import fr.frinn.custommachinery.api.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.common.requirement.DurabilityRequirement;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.OptionalString;

@ZenRegister
@Name(CTConstants.REQUIREMENT_DURABILITY)
public interface DurabilityRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T damageItem(IIngredient ingredient, int amount, @OptionalString String slot) {
        return addRequirement(new DurabilityRequirement(RequirementIOMode.INPUT, ingredient.asVanillaIngredient(), amount, true, slot));
    }

    @Method
    default T damageItemNoBreak(IIngredient ingredient, int amount, @OptionalString String slot) {
        return addRequirement(new DurabilityRequirement(RequirementIOMode.INPUT, ingredient.asVanillaIngredient(), amount, false, slot));
    }

    @Method
    default T repairItem(IIngredient ingredient, int amount, @OptionalString String slot) {
        return addRequirement(new DurabilityRequirement(RequirementIOMode.OUTPUT, ingredient.asVanillaIngredient(), amount, false, slot));
    }
}

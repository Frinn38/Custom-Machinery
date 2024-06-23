package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import fr.frinn.custommachinery.api.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.common.requirement.DropRequirement;
import net.minecraft.world.item.Items;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.OptionalBoolean;

@ZenRegister
@Name(CTConstants.REQUIREMENT_DROP)
public interface DropRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T checkDrop(IItemStack item, int radius) {
        return checkDrops(item, item.amount(), radius, true);
    }

    @Method
    default T checkAnyDrop(int amount, int radius) {
        return checkDrops(IItemStack.empty(), amount, radius, false);
    }

    @Method
    default T checkDrops(IIngredient items, int amount, int radius, @OptionalBoolean(true) boolean whitelist) {
        return addRequirement(new DropRequirement(RequirementIOMode.INPUT, DropRequirement.Action.CHECK, items.asVanillaIngredient(), whitelist, Items.AIR, amount, radius));
    }

    @Method
    default T consumeDropOnStart(IItemStack item, int radius) {
        return consumeDropsOnStart(item, item.amount(), radius, true);
    }

    @Method
    default T consumeAnyDropOnStart(int amount, int radius) {
        return consumeDropsOnStart(IItemStack.empty(), amount, radius, false);
    }

    @Method
    default T consumeDropsOnStart(IIngredient items, int amount, int radius, @OptionalBoolean(true) boolean whitelist) {
        return addRequirement(new DropRequirement(RequirementIOMode.INPUT, DropRequirement.Action.CONSUME, items.asVanillaIngredient(), whitelist, Items.AIR, amount, radius));
    }

    @Method
    default T consumeDropOnEnd(IItemStack item, int radius) {
        return consumeDropsOnEnd(item, item.amount(), radius, true);
    }

    @Method
    default T consumeAnyDropOnEnd(int amount, int radius) {
        return consumeDropsOnEnd(IItemStack.empty(), amount, radius, false);
    }

    @Method
    default T consumeDropsOnEnd(IIngredient ingredient, int amount, int radius, @OptionalBoolean(true) boolean whitelist) {
        return addRequirement(new DropRequirement(RequirementIOMode.OUTPUT, DropRequirement.Action.CONSUME, ingredient.asVanillaIngredient(), whitelist, Items.AIR, amount, radius));
    }

    @Method
    default T dropItemOnStart(IItemStack stack) {
        return addRequirement(new DropRequirement(RequirementIOMode.INPUT, DropRequirement.Action.PRODUCE, IItemStack.empty().asVanillaIngredient(), true, stack.getDefinition(), stack.amount(), 1));
    }

    @Method
    default T dropItemOnEnd(IItemStack stack) {
        return addRequirement(new DropRequirement(RequirementIOMode.OUTPUT, DropRequirement.Action.PRODUCE, IItemStack.empty().asVanillaIngredient(), true, stack.getDefinition(), stack.amount(), 1));
    }
}

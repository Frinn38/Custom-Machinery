package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTUtils;
import fr.frinn.custommachinery.common.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.common.requirement.DropRequirement;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import fr.frinn.custommachinery.common.util.ingredient.ItemIngredient;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.OptionalBoolean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ZenRegister
@Name(CTConstants.REQUIREMENT_DROP)
public interface DropRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T checkDrop(IItemStack item, int radius) {
        return checkDrops(new IItemStack[]{item}, item.getAmount(), radius, true);
    }

    @Method
    default T checkAnyDrop(int amount, int radius) {
        return checkDrops(new IItemStack[]{}, amount, radius, false);
    }

    @Method
    default T checkDrops(IItemStack[] items, int amount, int radius, @OptionalBoolean(true) boolean whitelist) {
        if(items.length == 0 && whitelist)
            return error("Invalid Drop requirement, checkDrop method must have at least 1 item defined when using whitelist mode");

        List<IIngredient<Item>> input = Arrays.stream(items).map(IItemStack::getDefinition).map(ItemIngredient::new).collect(Collectors.toList());
        return addRequirement(new DropRequirement(RequirementIOMode.INPUT, DropRequirement.Action.CHECK, input, whitelist, Items.AIR, CTUtils.nbtFromStack(items[0]), amount, radius));
    }

    @Method
    default T consumeDropOnStart(IItemStack item, int radius) {
        return consumeDropsOnStart(new IItemStack[]{item}, item.getAmount(), radius, true);
    }

    @Method
    default T consumeAnyDropOnStart(int amount, int radius) {
        return consumeDropsOnStart(new IItemStack[]{}, amount, radius, false);
    }

    @Method
    default T consumeDropsOnStart(IItemStack[] items, int amount, int radius, @OptionalBoolean(true) boolean whitelist) {
        if(items.length == 0 && whitelist)
            return error("Invalid Drop requirement, consumeDropOnStart method must have at least 1 item defined when using whitelist mode");

        List<IIngredient<Item>> input = Arrays.stream(items).map(IItemStack::getDefinition).map(ItemIngredient::new).collect(Collectors.toList());
        return addRequirement(new DropRequirement(RequirementIOMode.INPUT, DropRequirement.Action.CONSUME, input, whitelist, Items.AIR, CTUtils.nbtFromStack(items[0]), amount, radius));
    }

    @Method
    default T consumeDropOnEnd(IItemStack item, int radius) {
        return consumeDropsOnEnd(new IItemStack[]{item}, item.getAmount(), radius, true);
    }

    @Method
    default T consumeAnyDropOnEnd(int amount, int radius) {
        return consumeDropsOnEnd(new IItemStack[]{}, amount, radius, false);
    }

    @Method
    default T consumeDropsOnEnd(IItemStack[] items, int amount, int radius, @OptionalBoolean(true) boolean whitelist) {
        if(items.length == 0 && whitelist)
            return error("Invalid Drop requirement, consumeDropOnEnd method must have at least 1 item defined when using whitelist mode");

        List<IIngredient<Item>> input = Arrays.stream(items).map(IItemStack::getDefinition).map(ItemIngredient::new).collect(Collectors.toList());
        return addRequirement(new DropRequirement(RequirementIOMode.OUTPUT, DropRequirement.Action.CONSUME, input, whitelist, Items.AIR, CTUtils.nbtFromStack(items[0]), amount, radius));
    }

    @Method
    default T dropItemOnStart(IItemStack stack) {
        return addRequirement(new DropRequirement(RequirementIOMode.INPUT, DropRequirement.Action.PRODUCE, Collections.emptyList(), true, stack.getDefinition(), CTUtils.nbtFromStack(stack), stack.getAmount(), 1));
    }

    @Method
    default T dropItemOnEnd(IItemStack stack) {
        return addRequirement(new DropRequirement(RequirementIOMode.OUTPUT, DropRequirement.Action.PRODUCE, Collections.emptyList(), true, stack.getDefinition(), CTUtils.nbtFromStack(stack), stack.getAmount(), 1));
    }
}

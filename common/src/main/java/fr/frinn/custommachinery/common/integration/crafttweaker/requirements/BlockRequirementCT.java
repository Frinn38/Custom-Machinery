package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.api.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.common.requirement.BlockRequirement;
import fr.frinn.custommachinery.common.util.ComparatorMode;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import net.minecraft.world.phys.AABB;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.Optional;
import org.openzen.zencode.java.ZenCodeType.OptionalBoolean;
import org.openzen.zencode.java.ZenCodeType.OptionalInt;
import org.openzen.zencode.java.ZenCodeType.OptionalString;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@ZenRegister
@Name(CTConstants.REQUIREMENT_BLOCK)
public interface BlockRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T requireBlock(String[] filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount, @OptionalString(">=") String comparator) {
        return withBlockRequirement(RequirementIOMode.INPUT, BlockRequirement.ACTION.CHECK, "", startX, startY, startZ, endX, endY, endZ, amount, comparator, filter, whitelist);
    }

    @Method
    default T placeBlockOnStart(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount) {
        return withBlockRequirement(RequirementIOMode.INPUT, BlockRequirement.ACTION.PLACE, block, startX, startY, startZ, endX, endY, endZ, amount, "==", new String[]{}, false);
    }

    @Method
    default T placeBlockOnEnd(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount) {
        return withBlockRequirement(RequirementIOMode.OUTPUT, BlockRequirement.ACTION.PLACE, block, startX, startY, startZ, endX, endY, endZ, amount, "==", new String[]{}, false);
    }

    @Method
    default T breakAndPlaceBlockOnStart(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount, @Optional String[] filter, @OptionalBoolean boolean whitelist) {
        return withBlockRequirement(RequirementIOMode.INPUT, BlockRequirement.ACTION.REPLACE_BREAK, block, startX, startY, startZ, endX, endY, endZ, amount, "==", filter, whitelist);
    }

    @Method
    default T breakAndPlaceBlockOnEnd(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount, @Optional String[] filter, @OptionalBoolean boolean whitelist) {
        return withBlockRequirement(RequirementIOMode.OUTPUT, BlockRequirement.ACTION.REPLACE_BREAK, block, startX, startY, startZ, endX, endY, endZ, amount, "==", filter, whitelist);
    }

    @Method
    default T destroyAndPlaceBlockOnStart(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount, @Optional String[] filter, @OptionalBoolean boolean whitelist) {
        return withBlockRequirement(RequirementIOMode.INPUT, BlockRequirement.ACTION.REPLACE_DESTROY, block, startX, startY, startZ, endX, endY, endZ, amount, "==", filter, whitelist);
    }

    @Method
    default T destroyAndPlaceBlockOnEnd(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount, @Optional String[] filter, @OptionalBoolean boolean whitelist) {
        return withBlockRequirement(RequirementIOMode.OUTPUT, BlockRequirement.ACTION.REPLACE_DESTROY, block, startX, startY, startZ, endX, endY, endZ, amount, "==", filter, whitelist);
    }

    @Method
    default T destroyBlockOnStart(String[] filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount) {
        return withBlockRequirement(RequirementIOMode.INPUT, BlockRequirement.ACTION.DESTROY, "", startX, startY, startZ, endX, endY, endZ, amount, "==", filter, whitelist);
    }

    @Method
    default T destroyBlockOnEnd(String[] filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount) {
        return withBlockRequirement(RequirementIOMode.OUTPUT, BlockRequirement.ACTION.DESTROY, "", startX, startY, startZ, endX, endY, endZ, amount, "==", filter, whitelist);
    }

    @Method
    default T breakBlockOnStart(String[] filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount) {
        return withBlockRequirement(RequirementIOMode.INPUT, BlockRequirement.ACTION.BREAK, "", startX, startY, startZ, endX, endY, endZ, amount, "==", filter, whitelist);
    }

    @Method
    default T breakBlockOnEnd(String[] filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount) {
        return withBlockRequirement(RequirementIOMode.OUTPUT, BlockRequirement.ACTION.BREAK, "", startX, startY, startZ, endX, endY, endZ, amount, "==", filter, whitelist);
    }

    private T withBlockRequirement(RequirementIOMode mode, BlockRequirement.ACTION action, String block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount, String comparator, String[] stringFilter, boolean whitelist) {
        PartialBlockState state;
        if(block.isEmpty())
            state = PartialBlockState.AIR;
        else
            state = PartialBlockState.CODEC.read(JsonOps.INSTANCE, new JsonPrimitive(block)).resultOrPartial(CraftTweakerAPI.LOGGER::error).orElse(null);
        if(state == null)
            return error("Invalid block: {}", block);

        AABB bb = new AABB(startX, startY, startZ, endX, endY, endZ);
        List<IIngredient<PartialBlockState>> filter;
        if(stringFilter != null)
            filter = Arrays.stream(stringFilter).map(s -> IIngredient.BLOCK.read(JsonOps.INSTANCE, new JsonPrimitive(s)).resultOrPartial(CraftTweakerAPI.LOGGER::error).orElse(null)).filter(Objects::nonNull).toList();
        else
            filter = Collections.emptyList();
        try {
            return this.addRequirement(new BlockRequirement(mode, action, bb, amount, ComparatorMode.value(comparator), state, filter, whitelist));
        } catch (IllegalArgumentException e) {
            return error("Invalid comparator: {}", comparator);
        }
    }
}

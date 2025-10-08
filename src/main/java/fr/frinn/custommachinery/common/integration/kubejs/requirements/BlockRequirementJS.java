package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.requirement.BlockRequirement;
import fr.frinn.custommachinery.common.requirement.BlockRequirement.Action;
import fr.frinn.custommachinery.common.util.BlockIngredient;
import fr.frinn.custommachinery.common.util.ComparatorMode;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import net.minecraft.world.phys.AABB;

import java.util.Collections;
import java.util.List;

public interface BlockRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireBlock(List<BlockIngredient> filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        return this.requireBlock(filter, whitelist, startX, startY, startZ, endX, endY, endZ, 1, ComparatorMode.GREATER_OR_EQUALS);
    }

    default RecipeJSBuilder requireBlock(List<BlockIngredient> filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount) {
        return this.requireBlock(filter, whitelist, startX, startY, startZ, endX, endY, endZ, amount, ComparatorMode.GREATER_OR_EQUALS);
    }

    default RecipeJSBuilder requireBlock(List<BlockIngredient> filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount, ComparatorMode comparator) {
        return this.blockRequirement(RequirementIOMode.INPUT, Action.CHECK, PartialBlockState.AIR, startX, startY, startZ, endX, endY, endZ, amount, comparator, filter, whitelist);
    }

    default RecipeJSBuilder placeBlockOnStart(PartialBlockState block, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        return this.placeBlockOnStart(block, startX, startY, startZ, endX, endY, endZ, 1);
    }

    default RecipeJSBuilder placeBlockOnStart(PartialBlockState block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount) {
        return this.blockRequirement(RequirementIOMode.INPUT, Action.PLACE, block, startX, startY, startZ, endX, endY, endZ, amount, ComparatorMode.EQUALS, Collections.emptyList(), true);
    }

    default RecipeJSBuilder placeBlockOnEnd(PartialBlockState block, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        return this.placeBlockOnEnd(block, startX, startY, startZ, endX, endY, endZ, 1);
    }

    default RecipeJSBuilder placeBlockOnEnd(PartialBlockState block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount) {
        return this.blockRequirement(RequirementIOMode.OUTPUT, Action.PLACE, block, startX, startY, startZ, endX, endY, endZ, amount, ComparatorMode.EQUALS, Collections.emptyList(), true);
    }

    default RecipeJSBuilder breakAndPlaceBlockOnStart(PartialBlockState block, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        return this.breakAndPlaceBlockOnStart(block, startX, startY, startZ, endX, endY, endZ, 1);
    }

    default RecipeJSBuilder breakAndPlaceBlockOnStart(PartialBlockState block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount) {
        return this.breakAndPlaceBlockOnStart(block, startX, startY, startZ, endX, endY, endZ, amount, Collections.emptyList());
    }

    default RecipeJSBuilder breakAndPlaceBlockOnStart(PartialBlockState block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount, List<BlockIngredient> filter) {
        return this.breakAndPlaceBlockOnStart(block, startX, startY, startZ, endX, endY, endZ, amount, filter, false);
    }

    default RecipeJSBuilder breakAndPlaceBlockOnStart(PartialBlockState block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount, List<BlockIngredient> filter, boolean whitelist) {
        return this.blockRequirement(RequirementIOMode.INPUT, Action.REPLACE_BREAK, block, startX, startY, startZ, endX, endY, endZ, amount, ComparatorMode.EQUALS, filter, whitelist);
    }

    default RecipeJSBuilder breakAndPlaceBlockOnEnd(PartialBlockState block, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        return this.breakAndPlaceBlockOnEnd(block, startX, startY, startZ, endX, endY, endZ, 1);
    }

    default RecipeJSBuilder breakAndPlaceBlockOnEnd(PartialBlockState block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount) {
        return this.breakAndPlaceBlockOnEnd(block, startX, startY, startZ, endX, endY, endZ, amount, Collections.emptyList());
    }

    default RecipeJSBuilder breakAndPlaceBlockOnEnd(PartialBlockState block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount, List<BlockIngredient> filter) {
        return this.breakAndPlaceBlockOnEnd(block, startX, startY, startZ, endX, endY, endZ, amount, filter, false);
    }

    default RecipeJSBuilder breakAndPlaceBlockOnEnd(PartialBlockState block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount, List<BlockIngredient> filter, boolean whitelist) {
        return this.blockRequirement(RequirementIOMode.OUTPUT, Action.REPLACE_BREAK, block, startX, startY, startZ, endX, endY, endZ, amount, ComparatorMode.EQUALS, filter, whitelist);
    }

    default RecipeJSBuilder destroyAndPlaceBlockOnStart(PartialBlockState block, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        return this.destroyAndPlaceBlockOnStart(block, startX, startY, startZ, endX, endY, endZ, 1);
    }

    default RecipeJSBuilder destroyAndPlaceBlockOnStart(PartialBlockState block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount) {
        return this.destroyAndPlaceBlockOnStart(block, startX, startY, startZ, endX, endY, endZ, amount, Collections.emptyList());
    }

    default RecipeJSBuilder destroyAndPlaceBlockOnStart(PartialBlockState block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount, List<BlockIngredient> filter) {
        return this.destroyAndPlaceBlockOnStart(block, startX, startY, startZ, endX, endY, endZ, amount, filter, false);
    }

    default RecipeJSBuilder destroyAndPlaceBlockOnStart(PartialBlockState block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount, List<BlockIngredient> filter, boolean whitelist) {
        return this.blockRequirement(RequirementIOMode.INPUT, Action.REPLACE_DESTROY, block, startX, startY, startZ, endX, endY, endZ, amount, ComparatorMode.EQUALS, filter, whitelist);
    }

    default RecipeJSBuilder destroyAndPlaceBlockOnEnd(PartialBlockState block, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        return this.destroyAndPlaceBlockOnEnd(block, startX, startY, startZ, endX, endY, endZ, 1);
    }

    default RecipeJSBuilder destroyAndPlaceBlockOnEnd(PartialBlockState block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount) {
        return this.destroyAndPlaceBlockOnEnd(block, startX, startY, startZ, endX, endY, endZ, amount, Collections.emptyList());
    }

    default RecipeJSBuilder destroyAndPlaceBlockOnEnd(PartialBlockState block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount, List<BlockIngredient> filter) {
        return this.destroyAndPlaceBlockOnEnd(block, startX, startY, startZ, endX, endY, endZ, amount, filter, false);
    }

    default RecipeJSBuilder destroyAndPlaceBlockOnEnd(PartialBlockState block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount, List<BlockIngredient> filter, boolean whitelist) {
        return this.blockRequirement(RequirementIOMode.OUTPUT, Action.REPLACE_DESTROY, block, startX, startY, startZ, endX, endY, endZ, amount, ComparatorMode.EQUALS, filter, whitelist);
    }

    default RecipeJSBuilder destroyBlockOnStart(List<BlockIngredient> filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        return this.destroyBlockOnStart(filter, whitelist, startX, startY, startZ, endX, endY, endZ, 1);
    }

    default RecipeJSBuilder destroyBlockOnStart(List<BlockIngredient> filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount) {
        return this.blockRequirement(RequirementIOMode.INPUT, Action.DESTROY, PartialBlockState.AIR, startX, startY, startZ, endX, endY, endZ, amount, ComparatorMode.EQUALS, filter, whitelist);
    }

    default RecipeJSBuilder destroyBlockOnEnd(List<BlockIngredient> filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        return this.destroyBlockOnEnd(filter, whitelist, startX, startY, startZ, endX, endY, endZ, 1);
    }

    default RecipeJSBuilder destroyBlockOnEnd(List<BlockIngredient> filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount) {
        return this.blockRequirement(RequirementIOMode.OUTPUT, Action.DESTROY, PartialBlockState.AIR, startX, startY, startZ, endX, endY, endZ, amount, ComparatorMode.EQUALS, filter, whitelist);
    }

    default RecipeJSBuilder breakBlockOnStart(List<BlockIngredient> filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        return this.breakBlockOnStart(filter, whitelist, startX, startY, startZ, endX, endY, endZ, 1);
    }

    default RecipeJSBuilder breakBlockOnStart(List<BlockIngredient> filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount) {
        return this.blockRequirement(RequirementIOMode.INPUT, Action.BREAK, PartialBlockState.AIR, startX, startY, startZ, endX, endY, endZ, amount, ComparatorMode.EQUALS, filter, whitelist);
    }

    default RecipeJSBuilder breakBlockOnEnd(List<BlockIngredient> filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        return this.breakBlockOnEnd(filter, whitelist, startX, startY, startZ, endX, endY, endZ, 1);
    }

    default RecipeJSBuilder breakBlockOnEnd(List<BlockIngredient> filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount) {
        return this.blockRequirement(RequirementIOMode.OUTPUT, Action.BREAK, PartialBlockState.AIR, startX, startY, startZ, endX, endY, endZ, amount, ComparatorMode.EQUALS, filter, whitelist);
    }

    default RecipeJSBuilder blockRequirement(RequirementIOMode mode, Action action, PartialBlockState state, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount, ComparatorMode comparator, List<BlockIngredient> filter, boolean whitelist) {
        AABB bb = new AABB(startX, startY, startZ, endX, endY, endZ);
        try {
            return this.addRequirement(new BlockRequirement(mode, action, bb, amount, comparator, state, filter, whitelist));
        } catch (IllegalArgumentException e) {
            return error("Invalid comparator: {}", comparator);
        }
    }
}

package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.StructureRequirement;
import fr.frinn.custommachinery.common.requirement.StructureRequirement.Action;
import fr.frinn.custommachinery.common.util.BlockIngredient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public interface StructureRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder destroyStructure(String[][] pattern, Map<Character, List<BlockIngredient>> keys) {
        return requireStructure(pattern, keys, Action.DESTROY);
    }

    default RecipeJSBuilder breakStructure(String[][] pattern, Map<Character, List<BlockIngredient>> keys) {
        return requireStructure(pattern, keys, Action.BREAK);
    }

    default RecipeJSBuilder requireStructure(String[][] pattern, Map<Character, List<BlockIngredient>> keys) {
        return requireStructure(pattern, keys, Action.CHECK);
    }

    default RecipeJSBuilder placeStructure(String[][] pattern, Map<Character, List<BlockIngredient>> keys, boolean drops) {
        return requireStructure(pattern, keys, drops ? Action.PLACE_BREAK : Action.PLACE_DESTROY);
    }

    default RecipeJSBuilder requireStructure(String[][] pattern, Map<Character, List<BlockIngredient>> keys, Action action) {
        List<List<String>> patternList = Arrays.stream(pattern).map(floors -> Arrays.stream(floors).toList()).toList();
        try {
            return addRequirement(new StructureRequirement(patternList, keys, action));
        } catch (IllegalStateException e) {
            return error("Error while creating structure requirement: {}\nPattern: {}\nKeys: {}", e.getMessage(), pattern, keys);
        }
    }
}

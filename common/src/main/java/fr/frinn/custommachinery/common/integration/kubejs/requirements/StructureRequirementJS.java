package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import com.google.gson.JsonPrimitive;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.StructureRequirement;
import fr.frinn.custommachinery.common.requirement.StructureRequirement.Action;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface StructureRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder destroyStructure(String[][] pattern, Map<String, String> keys) {
        return requireStructure(pattern, keys, Action.DESTROY);
    }

    default RecipeJSBuilder breakStructure(String[][] pattern, Map<String, String> keys) {
        return requireStructure(pattern, keys, Action.BREAK);
    }

    default RecipeJSBuilder requireStructure(String[][] pattern, Map<String, String> keys) {
        return requireStructure(pattern, keys, Action.CHECK);
    }

    default RecipeJSBuilder placeStructure(String[][] pattern, Map<String, String> keys, boolean drops) {
        return requireStructure(pattern, keys, drops ? Action.PLACE_BREAK : Action.PLACE_DESTROY);
    }

    default RecipeJSBuilder requireStructure(String[][] pattern, Map<String, String> keys, Action action) {
        List<List<String>> patternList = Arrays.stream(pattern).map(floors -> Arrays.stream(floors).toList()).toList();
        Map<Character, IIngredient<PartialBlockState>> keysMap = new HashMap<>();
        for(Map.Entry<String, String> entry : keys.entrySet()) {
            if(entry.getKey().length() != 1)
                return error("Invalid structure key: \"{}\" must be a single character which is not 'm'", entry.getKey());

            char keyChar = entry.getKey().charAt(0);
            DataResult<IIngredient<PartialBlockState>> result = IIngredient.BLOCK.read(JsonOps.INSTANCE, new JsonPrimitive(entry.getValue()));
            if(result.error().isPresent() || result.result().isEmpty())
                return error("Invalid structure block: \"{}\", {}", entry.getValue(), result.error().get().message());

            keysMap.put(keyChar, result.result().get());
        }
        try {
            return addRequirement(new StructureRequirement(patternList, keysMap, action));
        } catch (IllegalStateException e) {
            return error("Error while creating structure requirement: {}\nPattern: {}\nKeys: {}", e.getMessage(), pattern, keys);
        }
    }
}

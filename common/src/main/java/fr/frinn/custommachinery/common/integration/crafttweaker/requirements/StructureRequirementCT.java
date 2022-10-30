package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.common.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.common.requirement.StructureRequirement;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ZenRegister
@Name(CTConstants.REQUIREMENT_STRUCTURE)
public interface StructureRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T requireStructure(String[][] pattern, Map<String, String> key) {
        List<List<String>> patternList = Arrays.stream(pattern).map(floors -> Arrays.stream(floors).toList()).toList();
        Map<Character, IIngredient<PartialBlockState>> keysMap = new HashMap<>();
        for(Map.Entry<String, String> entry : key.entrySet()) {
            if(entry.getKey().length() != 1)
                return error("Invalid structure key: {}\nMust be a single character which is not 'm'", entry.getKey());

            char keyChar = entry.getKey().charAt(0);
            DataResult<IIngredient<PartialBlockState>> result = IIngredient.BLOCK.parse(JsonOps.INSTANCE, new JsonPrimitive(entry.getValue()));
            if(result.error().isPresent() || result.result().isEmpty())
                return error("Invalid structure block: {}\n{}", entry.getValue(), result.error().get().message());

            keysMap.put(keyChar, result.result().get());
        }
        return addRequirement(new StructureRequirement(patternList, keysMap));
    }
}

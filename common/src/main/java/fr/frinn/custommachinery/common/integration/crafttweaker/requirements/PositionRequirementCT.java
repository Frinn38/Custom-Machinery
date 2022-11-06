package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.common.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.common.requirement.PositionRequirement;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.PositionComparator;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@ZenRegister
@Name(CTConstants.REQUIREMENT_POSITION)
public interface PositionRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T requirePosition(String[] positions) {
        List<PositionComparator> positionComparators = Stream.of(positions).map(s -> Codecs.POSITION_COMPARATOR_CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(s)).resultOrPartial(CraftTweakerAPI.LOGGER::error).orElseThrow(() -> new IllegalArgumentException("Invalid position comparator: " + s))).toList();
        if(!positionComparators.isEmpty())
            return addRequirement(new PositionRequirement(positionComparators));
        return error("No valid position requirements found in array: {}", Arrays.toString(positions));
    }

    @Method
    default T requirePosition(String position) {
        PositionComparator positionComparator = Codecs.POSITION_COMPARATOR_CODEC.decode(JsonOps.INSTANCE, new JsonPrimitive(position)).resultOrPartial(CraftTweakerAPI.LOGGER::error).orElseThrow(() -> new IllegalArgumentException("Invalid position comparator: " + position)).getFirst();
        return addRequirement(new PositionRequirement(Collections.singletonList(positionComparator)));
    }
}

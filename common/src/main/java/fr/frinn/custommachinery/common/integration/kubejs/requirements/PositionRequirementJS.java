package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.script.ScriptType;
import fr.frinn.custommachinery.common.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.PositionRequirement;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.PositionComparator;

import java.util.List;
import java.util.stream.Stream;

public interface PositionRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requirePosition(String[] position) {
        List<PositionComparator> positionComparators = Stream.of(position).map(s -> Codecs.POSITION_COMPARATOR_CODEC.decode(JsonOps.INSTANCE, new JsonPrimitive(s)).resultOrPartial(ScriptType.SERVER.console::error).orElseThrow(() -> new IllegalArgumentException("Invalid position comparator: " + s)).getFirst()).toList();
        if(!positionComparators.isEmpty())
            return this.addRequirement(new PositionRequirement(positionComparators));
        return this;
    }
}

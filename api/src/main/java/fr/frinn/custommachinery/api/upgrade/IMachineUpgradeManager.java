package fr.frinn.custommachinery.api.upgrade;

import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IMachineUpgradeManager {

    /**
     * Mark the cached upgrades list as dirty.
     * Should be called each time the content of an upgrade slot changed.
     */
    void markDirty();

    List<Pair<IRecipeModifier, Integer>> getModifiers(RequirementType<?> type, @Nullable String target, @Nullable RequirementIOMode mode);
}

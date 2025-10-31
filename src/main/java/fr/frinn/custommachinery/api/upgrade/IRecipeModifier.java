package fr.frinn.custommachinery.api.upgrade;

import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public interface IRecipeModifier {

    boolean shouldApply(RequirementType<?> type, RequirementIOMode mode, @Nullable String target);

    double apply(double original, int upgradeAmount);

    Component tooltip();

    Component getDefaultTooltip();
}

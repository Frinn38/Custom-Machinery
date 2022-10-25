package fr.frinn.custommachinery.api.upgrade;

import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public interface IRecipeModifier {

    RequirementType<?> getRequirementType();

    String getTarget();

    RequirementIOMode getMode();

    OPERATION getOperation();

    double getModifier();

    double getChance();

    Component getTooltip();

    enum OPERATION {
        ADDITION,
        MULTIPLICATION;

        public static OPERATION value(String value) {
            return valueOf(value.toUpperCase(Locale.ROOT));
        }
    }
}

package fr.frinn.custommachinery.api.upgrade;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public interface IRecipeModifier {

    boolean shouldApply(RequirementType<?> type, RequirementIOMode mode, @Nullable String target);

    double apply(double original, int upgradeAmount);

    Component getTooltip();

    Component getDefaultTooltip();

    enum OPERATION {
        ADDITION,
        MULTIPLICATION,
        EXPONENTIAL;

        public static final NamedCodec<OPERATION> CODEC = NamedCodec.enumCodec(OPERATION.class);

        public static OPERATION value(String value) {
            return valueOf(value.toUpperCase(Locale.ROOT));
        }
    }
}

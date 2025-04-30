package fr.frinn.custommachinery.common.upgrade.modifier;

import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.upgrade.RecipeModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class ExponentialRecipeModifier extends RecipeModifier {

    public ExponentialRecipeModifier(RequirementType<?> requirementType, RequirementIOMode mode, double modifier, String target, double chance, double max, double min, @Nullable Component tooltip) {
        super(requirementType, mode, modifier, target, chance, max, min, tooltip);
    }

    @Override
    public double apply(double original, int upgradeAmount) {
        return Mth.clamp(original * Math.pow(this.modifier, upgradeAmount), this.min, this.max);
    }

    @Override
    public Component getDefaultTooltip() {
        double tooltipModifier = this.modifier * 100 - 100;
        return Component.literal((tooltipModifier >= 0 ? "+" : "") + tooltipModifier + "%" + " ")
                .append(this.requirementType.getName())
                .append(" ")
                .append(Component.translatable(this.mode.getTranslationKey()));
    }

    @Override
    public OPERATION getOperation() {
        return OPERATION.EXPONENTIAL;
    }
}

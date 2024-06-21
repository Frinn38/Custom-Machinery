package fr.frinn.custommachinery.common.upgrade.modifier;

import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.upgrade.RecipeModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class MultiplicationRecipeModifier extends RecipeModifier {

    public MultiplicationRecipeModifier(RequirementType<?> requirementType, RequirementIOMode mode, double modifier, String target, double chance, double max, double min, @Nullable Component tooltip) {
        super(requirementType, mode, modifier, target, chance, max, min, tooltip);
    }

    @Override
    public double apply(double original, int upgradeAmount) {
        return Mth.clamp(original * this.modifier * upgradeAmount, this.min, this.max);
    }

    @Override
    public Component getDefaultTooltip() {
        double tooltipModifier = this.modifier * 100 - 100;
        String tooltip = (tooltipModifier >= 0 ? "+" : "") + tooltipModifier + "%" +
                " " +
                this.requirementType.getName().getString() +
                " " +
                Component.translatable(this.mode.getTranslationKey()).getString();
        return Component.literal(tooltip);
    }

    @Override
    public OPERATION getOperation() {
        return OPERATION.MULTIPLICATION;
    }
}

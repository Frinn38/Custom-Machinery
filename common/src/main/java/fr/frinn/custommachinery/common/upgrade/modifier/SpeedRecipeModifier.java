package fr.frinn.custommachinery.common.upgrade.modifier;

import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.upgrade.RecipeModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class SpeedRecipeModifier extends RecipeModifier {

    private final OPERATION operation;

    public SpeedRecipeModifier(OPERATION operation, double modifier, double chance, double max, double min, @Nullable Component tooltip) {
        super(Registration.SPEED_REQUIREMENT.get(), RequirementIOMode.INPUT, modifier, "", chance, max, min, tooltip);
        this.operation = operation;
    }

    @Override
    public boolean shouldApply(RequirementType<?> type, RequirementIOMode mode, @Nullable String target) {
        return type == this.requirementType
                && (this.target.isEmpty() || this.target.equals(target))
                && this.chance > RAND.nextDouble();
    }

    @Override
    public double apply(double original, int upgradeAmount) {
        double modified = switch (this.operation) {
            case ADDITION -> original + this.modifier * upgradeAmount;
            case MULTIPLICATION -> original * this.modifier * upgradeAmount;
            case EXPONENTIAL -> original * Math.pow(this.modifier, upgradeAmount);
        };
        return Mth.clamp(modified, this.min, this.max);
    }

    @Override
    public Component getDefaultTooltip() {
        double tooltipModifier = this.operation == OPERATION.ADDITION ? this.modifier : this.modifier * 100 - 100;
        String tooltip = (tooltipModifier >= 0 ? "+" : "") + (this.operation == OPERATION.ADDITION ? tooltipModifier : tooltipModifier + "%") +
                " " +
                this.requirementType.getName().getString();
        return new TextComponent(tooltip);
    }

    @Override
    public OPERATION getOperation() {
        return this.operation;
    }
}

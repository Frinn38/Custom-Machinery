package fr.frinn.custommachinery.common.upgrade;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.api.upgrade.IRecipeModifier;
import fr.frinn.custommachinery.api.upgrade.Operation;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.codec.RegistrarCodec;
import fr.frinn.custommachinery.impl.util.TextComponentUtils;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Random;

public record RecipeModifier(RequirementType<?> requirementType, RequirementIOMode mode, String target,
                             Operation operation, double modifier, double chance, double max, double min,
                             Component tooltip) implements IRecipeModifier {

    public static final NamedCodec<RecipeModifier> CODEC = NamedCodec.record(modifierInstance ->
            modifierInstance.group(
                    RegistrarCodec.REQUIREMENT.fieldOf("requirement").forGetter(modifier -> modifier.requirementType),
                    RequirementIOMode.CODEC.fieldOf("mode").forGetter(modifier -> modifier.mode),
                    NamedCodec.STRING.optionalFieldOf("target", "").forGetter(modifier -> modifier.target),
                    Operation.CODEC.fieldOf("operation").forGetter(modifier -> modifier.operation),
                    NamedCodec.DOUBLE.fieldOf("modifier").forGetter(modifier -> modifier.modifier),
                    NamedCodec.DOUBLE.optionalFieldOf("chance", 1.0D).forGetter(modifier -> modifier.chance),
                    NamedCodec.DOUBLE.optionalFieldOf("max", Double.POSITIVE_INFINITY).forGetter(modifier -> modifier.max),
                    NamedCodec.DOUBLE.optionalFieldOf("min", Double.NEGATIVE_INFINITY).forGetter(modifier -> modifier.min),
                    TextComponentUtils.CODEC.optionalFieldOf("tooltip", Component.empty()).forGetter(modifier -> modifier.tooltip)
            ).apply(modifierInstance, RecipeModifier::new), "Recipe modifier"
    );

    public static final Random RAND = new Random();

    public RecipeModifier(RequirementType<?> requirementType, RequirementIOMode mode, String target, Operation operation, double modifier, double chance, double max, double min, Component tooltip) {
        this.requirementType = requirementType;
        this.mode = mode;
        this.target = target;
        this.operation = operation;
        this.modifier = modifier;
        this.chance = chance;
        this.max = max;
        this.min = min;
        this.tooltip = tooltip != null && !tooltip.getString().isEmpty() ? tooltip : getDefaultTooltip();
    }

    @Override
    public boolean shouldApply(RequirementType<?> type, RequirementIOMode mode, @Nullable String target) {
        if (this.requirementType == Registration.SPEED_REQUIREMENT.get() && type == Registration.SPEED_REQUIREMENT.get())
            return true;
        return type == this.requirementType
                && mode == this.mode
                && (this.target.isEmpty() && target == null) || this.target.equals(target)
                && this.chance > RAND.nextDouble();
    }

    @Override
    public double apply(double original, int upgradeAmount) {
        return this.operation.apply(original, this.modifier, upgradeAmount, this.min, this.max);
    }

    @Override
    public Component getDefaultTooltip() {
        if (this.requirementType == Registration.SPEED_REQUIREMENT.get()) {
            BigDecimal tooltipModifier = this.operation == Operation.ADDITION ? new BigDecimal(this.modifier) : new BigDecimal("" + this.modifier).multiply(new BigDecimal("100")).add(new BigDecimal("-100")).stripTrailingZeros();
            return Component.literal((tooltipModifier.intValue() >= 0 ? "+" : "") + (this.operation == Operation.ADDITION ? tooltipModifier.toPlainString() : tooltipModifier.toPlainString() + "%"))
                    .append(" ")
                    .append(this.requirementType.getName());
        }
        return switch (this.operation) {
            case ADDITION -> Component.literal((this.modifier >= 0 ? "+" : "") + new BigDecimal(this.modifier).stripTrailingZeros().toPlainString() + " ")
                    .append(this.requirementType.getName())
                    .append(" ")
                    .append(Component.translatable(this.mode.getTranslationKey()));
            case MULTIPLICATION, EXPONENTIAL -> {
                BigDecimal tooltipModifier = new BigDecimal("" + this.modifier).multiply(new BigDecimal("100")).add(new BigDecimal("-100")).stripTrailingZeros();
                yield Component.literal((tooltipModifier.intValue() >= 0 ? "+" : "") + tooltipModifier.toPlainString() + "%" + " ")
                        .append(this.requirementType.getName())
                        .append(" ")
                        .append(Component.translatable(this.mode.getTranslationKey()));
            }
        };
    }
}

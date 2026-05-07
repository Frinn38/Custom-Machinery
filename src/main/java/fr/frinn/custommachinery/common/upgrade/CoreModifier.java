package fr.frinn.custommachinery.common.upgrade;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.upgrade.Operation;
import fr.frinn.custommachinery.impl.util.TextComponentUtils;
import net.minecraft.network.chat.Component;

import java.math.BigDecimal;

public record CoreModifier(Operation operation, double modifier, int max, int min, Component tooltip) {

    public static final NamedCodec<CoreModifier> CODEC = NamedCodec.record(modifierInstance ->
            modifierInstance.group(
                    Operation.CODEC.fieldOf("operation").forGetter(modifier -> modifier.operation),
                    NamedCodec.DOUBLE.fieldOf("modifier").forGetter(modifier -> modifier.modifier),
                    NamedCodec.intRange(1, 32).optionalFieldOf("max", 32).forGetter(modifier -> modifier.max),
                    NamedCodec.intRange(1, 32).optionalFieldOf("min", 1).forGetter(modifier -> modifier.min),
                    TextComponentUtils.CODEC.optionalFieldOf("tooltip", Component.empty()).forGetter(modifier -> modifier.tooltip)
            ).apply(modifierInstance, CoreModifier::new), "Core modifier"
    );

    public CoreModifier(Operation operation, double modifier, int max, int min, Component tooltip) {
        this.operation = operation;
        this.modifier = modifier;
        this.max = max;
        this.min = min;
        this.tooltip = tooltip != null && !tooltip.getString().isEmpty() ? tooltip : getDefaultTooltip();
    }

    public double apply(double original, int upgradeAmount) {
        return this.operation.apply(original, this.modifier, upgradeAmount, this.min, this.max);
    }

    private Component getDefaultTooltip() {
        return switch (this.operation) {
            case ADDITION -> Component.literal((this.modifier >= 0 ? "+" : "") + new BigDecimal(this.modifier).stripTrailingZeros().toPlainString() + " cores");
            case MULTIPLICATION, EXPONENTIAL -> {
                BigDecimal tooltipModifier = new BigDecimal("" + this.modifier).multiply(new BigDecimal("100")).add(new BigDecimal("-100")).stripTrailingZeros();
                yield Component.literal((tooltipModifier.intValue() >= 0 ? "+" : "") + tooltipModifier.toPlainString() + "%" + " cores");
            }
        };
    }
}

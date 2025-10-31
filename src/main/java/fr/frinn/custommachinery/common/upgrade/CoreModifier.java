package fr.frinn.custommachinery.common.upgrade;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.upgrade.Operation;
import fr.frinn.custommachinery.impl.util.TextComponentUtils;
import net.minecraft.network.chat.Component;

public record CoreModifier(Operation operation, double modifier, double max, double min, Component tooltip) {

    public static final NamedCodec<CoreModifier> CODEC = NamedCodec.record(modifierInstance ->
            modifierInstance.group(
                    Operation.CODEC.fieldOf("operation").forGetter(modifier -> modifier.operation),
                    NamedCodec.DOUBLE.fieldOf("modifier").forGetter(modifier -> modifier.modifier),
                    NamedCodec.DOUBLE.optionalFieldOf("max", Double.POSITIVE_INFINITY).forGetter(modifier -> modifier.max),
                    NamedCodec.DOUBLE.optionalFieldOf("min", Double.NEGATIVE_INFINITY).forGetter(modifier -> modifier.min),
                    TextComponentUtils.CODEC.optionalFieldOf("tooltip", Component.empty()).forGetter(modifier -> modifier.tooltip)
            ).apply(modifierInstance, CoreModifier::new), "Core modifier"
    );

    public CoreModifier(Operation operation, double modifier, double max, double min, Component tooltip) {
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
            case ADDITION -> Component.literal((this.modifier >= 0 ? "+" : "") + this.modifier + " cores");
            case MULTIPLICATION, EXPONENTIAL -> {
                double tooltipModifier = this.modifier * 100 - 100;
                yield Component.literal((tooltipModifier >= 0 ? "+" : "") + tooltipModifier + "%" + " cores");
            }
        };
    }
}

package fr.frinn.custommachinery.common.upgrade;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.upgrade.Operation;
import fr.frinn.custommachinery.impl.codec.RegistrarCodec;
import fr.frinn.custommachinery.impl.util.TextComponentUtils;
import net.minecraft.network.chat.Component;

import java.math.BigDecimal;

public record ComponentModifier(MachineComponentType<?> component, String id, String target, Operation operation,
                                double modifier, double max, double min,
                                Component tooltip) {

    public static final NamedCodec<ComponentModifier> CODEC = NamedCodec.record(modifierInstance ->
            modifierInstance.group(
                    RegistrarCodec.MACHINE_COMPONENT.fieldOf("component").forGetter(modifier -> modifier.component),
                    NamedCodec.STRING.optionalFieldOf("id", "").forGetter(modifier -> modifier.id),
                    NamedCodec.STRING.fieldOf("target").forGetter(modifier -> modifier.target),
                    Operation.CODEC.fieldOf("operation").forGetter(modifier -> modifier.operation),
                    NamedCodec.DOUBLE.fieldOf("modifier").forGetter(modifier -> modifier.modifier),
                    NamedCodec.DOUBLE.optionalFieldOf("max", Double.POSITIVE_INFINITY).forGetter(modifier -> modifier.max),
                    NamedCodec.DOUBLE.optionalFieldOf("min", Double.NEGATIVE_INFINITY).forGetter(modifier -> modifier.min),
                    TextComponentUtils.CODEC.optionalFieldOf("tooltip", Component.empty()).forGetter(modifier -> modifier.tooltip)
            ).apply(modifierInstance, ComponentModifier::new), "Component modifier"
    );

    public ComponentModifier(MachineComponentType<?> component, String id, String target, Operation operation, double modifier, double max, double min, Component tooltip) {
        this.component = component;
        this.id = id;
        this.target = target;
        this.operation = operation;
        this.modifier = modifier;
        this.max = max;
        this.min = min;
        this.tooltip = !tooltip.getString().isEmpty() ? tooltip : getDefaultTooltip();
    }

    public double apply(double original, int upgradeAmount) {
        return this.operation.apply(original, this.modifier, upgradeAmount, this.min, this.max);
    }

    private Component getDefaultTooltip() {
        return switch (this.operation) {
            case ADDITION -> Component.literal((this.modifier >= 0 ? "+" : "") + new BigDecimal(this.modifier).stripTrailingZeros().toPlainString() + " ")
                    .append(this.component.getTranslatedName())
                    .append(" ")
                    .append(Component.literal(this.target));
            case MULTIPLICATION, EXPONENTIAL -> {
                BigDecimal tooltipModifier = new BigDecimal("" + this.modifier).multiply(new BigDecimal("100")).add(new BigDecimal("-100")).stripTrailingZeros();
                yield Component.literal((tooltipModifier.intValue() >= 0 ? "+" : "") + tooltipModifier.toPlainString() + "%" + " ")
                        .append(this.component.getTranslatedName())
                        .append(" ")
                        .append(Component.literal(this.target));
            }
        };
    }
}

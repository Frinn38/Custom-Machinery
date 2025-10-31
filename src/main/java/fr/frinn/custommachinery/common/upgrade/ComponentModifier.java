package fr.frinn.custommachinery.common.upgrade;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.upgrade.IComponentModifier;
import fr.frinn.custommachinery.api.upgrade.Operation;
import fr.frinn.custommachinery.impl.codec.RegistrarCodec;
import fr.frinn.custommachinery.impl.util.TextComponentUtils;
import net.minecraft.network.chat.Component;

public record ComponentModifier(MachineComponentType<?> component, String id, String target, Operation operation,
                                double modifier, double max, double min,
                                Component tooltip) implements IComponentModifier {

    public static final NamedCodec<ComponentModifier> CODEC = NamedCodec.record(modifierInstance ->
            modifierInstance.group(
                    RegistrarCodec.MACHINE_COMPONENT.fieldOf("component").forGetter(modifier -> modifier.component),
                    NamedCodec.STRING.optionalFieldOf("id", "").forGetter(modifier -> modifier.id),
                    NamedCodec.STRING.optionalFieldOf("target", "").forGetter(modifier -> modifier.target),
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
        this.tooltip = tooltip != null && !tooltip.getString().isEmpty() ? tooltip : getDefaultTooltip();
    }

    @Override
    public double apply(double original, int upgradeAmount) {
        return this.operation.apply(original, this.modifier, upgradeAmount, this.min, this.max);
    }

    private Component getDefaultTooltip() {
        return switch (this.operation) {
            case ADDITION -> Component.literal((this.modifier >= 0 ? "+" : "") + this.modifier + " ")
                    .append(this.component.getTranslatedName())
                    .append(" ")
                    .append(Component.literal(this.target));
            case MULTIPLICATION, EXPONENTIAL -> {
                double tooltipModifier = this.modifier * 100 - 100;
                yield Component.literal((tooltipModifier >= 0 ? "+" : "") + tooltipModifier + "%" + " ")
                        .append(this.component.getTranslatedName())
                        .append(" ")
                        .append(Component.literal(this.target));
            }
        };
    }
}

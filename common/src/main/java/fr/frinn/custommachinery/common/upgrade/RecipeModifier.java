package fr.frinn.custommachinery.common.upgrade;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.api.upgrade.IRecipeModifier;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.upgrade.modifier.AdditionRecipeModifier;
import fr.frinn.custommachinery.common.upgrade.modifier.ExponentialRecipeModifier;
import fr.frinn.custommachinery.common.upgrade.modifier.MultiplicationRecipeModifier;
import fr.frinn.custommachinery.common.upgrade.modifier.SpeedRecipeModifier;
import fr.frinn.custommachinery.common.util.TextComponentUtils;
import fr.frinn.custommachinery.impl.codec.RegistrarCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public abstract class RecipeModifier implements IRecipeModifier {

    public static final NamedCodec<RecipeModifier> CODEC = NamedCodec.record(energyModifierInstance ->
            energyModifierInstance.group(
                    RegistrarCodec.REQUIREMENT.fieldOf("requirement").forGetter(modifier -> modifier.requirementType),
                    RequirementIOMode.CODEC.fieldOf("mode").forGetter(modifier -> modifier.mode),
                    OPERATION.CODEC.fieldOf("operation").forGetter(RecipeModifier::getOperation),
                    NamedCodec.DOUBLE.fieldOf("modifier").forGetter(modifier -> modifier.modifier),
                    NamedCodec.STRING.optionalFieldOf("target", "").forGetter(modifier -> modifier.target),
                    NamedCodec.DOUBLE.optionalFieldOf("chance", 1.0D).forGetter(modifier -> modifier.chance),
                    NamedCodec.DOUBLE.optionalFieldOf("max", Double.POSITIVE_INFINITY).forGetter(modifier -> modifier.max),
                    NamedCodec.DOUBLE.optionalFieldOf("min", Double.NEGATIVE_INFINITY).forGetter(modifier -> modifier.min),
                    TextComponentUtils.CODEC.optionalFieldOf("tooltip", TextComponent.EMPTY).forGetter(modifier -> modifier.tooltip)
            ).apply(energyModifierInstance, (requirement, mode, operation, modifier, target, chance, max, min, tooltip) -> {
                    if(requirement == Registration.SPEED_REQUIREMENT.get())
                        return new SpeedRecipeModifier(operation, modifier, chance, max, min, tooltip);
                    return switch (operation) {
                        case ADDITION -> new AdditionRecipeModifier(requirement, mode, modifier, target, chance, max, min, tooltip);
                        case MULTIPLICATION -> new MultiplicationRecipeModifier(requirement, mode, modifier, target, chance, max, min, tooltip);
                        case EXPONENTIAL -> new ExponentialRecipeModifier(requirement, mode, modifier, target, chance, max, min, tooltip);
                    };
            }), "Recipe modifier"
    );

    public static final Random RAND = new Random();

    public final RequirementType<?> requirementType;
    public final String target;
    public final RequirementIOMode mode;
    public final double modifier;
    public final double chance;
    public final double max;
    public final double min;
    public final Component tooltip;

    public RecipeModifier(RequirementType<?> requirementType, RequirementIOMode mode, double modifier, String target, double chance, double max, double min, @Nullable Component tooltip) {
        this.requirementType = requirementType;
        this.target = target;
        this.mode = mode;
        this.modifier = modifier;
        this.chance = chance;
        this.max = max;
        this.min = min;
        this.tooltip = tooltip != null && tooltip != TextComponent.EMPTY ? tooltip : getDefaultTooltip();
    }

    @Override
    public boolean shouldApply(RequirementType<?> type, RequirementIOMode mode, @Nullable String target) {
        return type == this.requirementType
                && mode == this.mode
                && (this.target.isEmpty() || this.target.equals(target))
                && this.chance > RAND.nextDouble();
    }

    @Override
    public Component getTooltip() {
        return this.tooltip;
    }

    public abstract OPERATION getOperation();
}

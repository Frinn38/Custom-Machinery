package fr.frinn.custommachinery.common.upgrade;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.api.upgrade.IRecipeModifier;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.TextComponentUtils;
import fr.frinn.custommachinery.impl.codec.RegistrarCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;

public class RecipeModifier implements IRecipeModifier {

    public static final NamedCodec<RecipeModifier> CODEC = NamedCodec.record(energyModifierInstance ->
            energyModifierInstance.group(
                    RegistrarCodec.REQUIREMENT.fieldOf("requirement").forGetter(modifier -> modifier.requirementType),
                    RequirementIOMode.CODEC.fieldOf("mode").forGetter(modifier -> modifier.mode),
                    OPERATION.CODEC.fieldOf("operation").forGetter(modifier -> modifier.operation),
                    NamedCodec.DOUBLE.fieldOf("modifier").forGetter(modifier -> modifier.modifier),
                    NamedCodec.STRING.optionalFieldOf("target", "").forGetter(modifier -> modifier.target),
                    NamedCodec.DOUBLE.optionalFieldOf("chance", 1.0D).forGetter(modifier -> modifier.chance),
                    NamedCodec.DOUBLE.optionalFieldOf("max", Double.POSITIVE_INFINITY).forGetter(modifier -> modifier.max),
                    NamedCodec.DOUBLE.optionalFieldOf("min", Double.NEGATIVE_INFINITY).forGetter(modifier -> modifier.min),
                    TextComponentUtils.CODEC.optionalFieldOf("tooltip", TextComponent.EMPTY).forGetter(modifier -> modifier.tooltip)
            ).apply(energyModifierInstance, RecipeModifier::new), "Recipe modifier"
    );

    private final RequirementType<?> requirementType;
    private final String target;
    private final RequirementIOMode mode;
    private final OPERATION operation;
    private final double modifier;
    private final double chance;
    private final double max;
    private final double min;
    private final Component tooltip;

    public RecipeModifier(RequirementType<?> requirementType, RequirementIOMode mode, OPERATION operation, double modifier, String target, double chance, double max, double min, @Nullable Component tooltip) {
        this.requirementType = requirementType;
        this.target = target;
        this.mode = mode;
        this.operation = operation;
        this.modifier = modifier;
        this.chance = chance;
        this.max = max;
        this.min = min;
        this.tooltip = tooltip != null && tooltip != TextComponent.EMPTY ? tooltip : getDefaultTooltip(this);
    }

    @Override
    public RequirementType<?> getRequirementType() {
        return this.requirementType;
    }

    @Override
    public String getTarget() {
        return this.target;
    }

    @Override
    public RequirementIOMode getMode() {
        return this.mode;
    }

    @Override
    public OPERATION getOperation() {
        return this.operation;
    }

    @Override
    public double getModifier() {
        return this.modifier;
    }

    @Override
    public double getChance() {
        return this.chance;
    }

    @Override
    public Component getTooltip() {
        return this.tooltip;
    }

    public static Component getDefaultTooltip(RecipeModifier modifier) {
        double tooltipModifier = modifier.operation == OPERATION.ADDITION ? modifier.modifier : modifier.modifier * 100 - 100;
        StringBuilder tooltip = new StringBuilder(tooltipModifier >= 0 ? "+" : "");
        tooltip.append(modifier.operation == OPERATION.ADDITION ? tooltipModifier : tooltipModifier + "%");
        tooltip.append(" ");
        tooltip.append(modifier.requirementType.getName().getString());
        if(modifier.requirementType != Registration.SPEED_REQUIREMENT.get()) {
            tooltip.append(" ");
            tooltip.append(new TranslatableComponent(modifier.mode.getTranslationKey()).getString());
        }
        return new TextComponent(tooltip.toString());
    }
}

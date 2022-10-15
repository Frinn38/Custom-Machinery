package fr.frinn.custommachinery.common.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.TextComponentUtils;
import fr.frinn.custommachinery.impl.codec.CodecLogger;
import fr.frinn.custommachinery.impl.codec.RegistrarCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class RecipeModifier {

    public static final Codec<RecipeModifier> CODEC = RecordCodecBuilder.create(energyModifierInstance ->
            energyModifierInstance.group(
                    RegistrarCodec.REQUIREMENT.fieldOf("requirement").forGetter(modifier -> modifier.requirementType),
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(modifier -> modifier.mode),
                    Codecs.MODIFIER_OPERATION_CODEC.fieldOf("operation").forGetter(modifier -> modifier.operation),
                    Codec.DOUBLE.fieldOf("modifier").forGetter(modifier -> modifier.modifier),
                    CodecLogger.loggedOptional(Codec.STRING,"target", "").forGetter(modifier -> modifier.target),
                    CodecLogger.loggedOptional(Codec.DOUBLE,"chance", 1.0D).forGetter(modifier -> modifier.chance),
                    CodecLogger.loggedOptional(Codec.DOUBLE, "max", Double.POSITIVE_INFINITY).forGetter(modifier -> modifier.max),
                    CodecLogger.loggedOptional(Codec.DOUBLE, "min", Double.NEGATIVE_INFINITY).forGetter(modifier -> modifier.min),
                    CodecLogger.loggedOptional(TextComponentUtils.CODEC, "tooltip", TextComponent.EMPTY).forGetter(modifier -> modifier.tooltip)
            ).apply(energyModifierInstance, RecipeModifier::new)
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

    public RequirementType<?> getRequirementType() {
        return this.requirementType;
    }

    public String getTarget() {
        return this.target;
    }

    public RequirementIOMode getMode() {
        return this.mode;
    }

    public OPERATION getOperation() {
        return this.operation;
    }

    public double getModifier() {
        return this.modifier;
    }

    public double getChance() {
        return this.chance;
    }

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

    public enum OPERATION {
        ADDITION,
        MULTIPLICATION;

        public static OPERATION value(String value) {
            return valueOf(value.toUpperCase(Locale.ENGLISH));
        }
    }
}

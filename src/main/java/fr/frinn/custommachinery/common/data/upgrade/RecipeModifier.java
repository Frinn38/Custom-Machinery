package fr.frinn.custommachinery.common.data.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.utils.CodecLogger;
import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.crafting.requirements.RequirementType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class RecipeModifier {

    public static final Codec<RecipeModifier> CODEC = RecordCodecBuilder.create(energyModifierInstance ->
            energyModifierInstance.group(
                    Codecs.REQUIREMENT_TYPE.fieldOf("requirement").forGetter(modifier -> modifier.requirementType),
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(modifier -> modifier.mode),
                    Codecs.MODIFIER_OPERATION_CODEC.fieldOf("operation").forGetter(modifier -> modifier.operation),
                    Codec.DOUBLE.fieldOf("modifier").forGetter(modifier -> modifier.modifier),
                    CodecLogger.loggedOptional(Codec.STRING,"target", "").forGetter(modifier -> modifier.target),
                    CodecLogger.loggedOptional(Codec.DOUBLE,"chance", 1.0D).forGetter(modifier -> modifier.chance)
            ).apply(energyModifierInstance, RecipeModifier::new)
    );

    private RequirementType<?> requirementType;
    private String target;
    private IRequirement.MODE mode;
    private OPERATION operation;
    private double modifier;
    private double chance;

    public RecipeModifier(RequirementType<?> requirementType, IRequirement.MODE mode, OPERATION operation, double modifier, String target, double chance) {
        this.requirementType = requirementType;
        this.target = target;
        this.mode = mode;
        this.operation = operation;
        this.modifier = modifier;
        this.chance = chance;
    }

    public RequirementType<?> getRequirementType() {
        return this.requirementType;
    }

    public String getTarget() {
        return this.target;
    }

    public IRequirement.MODE getMode() {
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

    public List<ITextComponent> getTooltip() {
        double tooltipModifier = this.operation == OPERATION.ADDITION ? this.modifier : this.modifier * 100 - 100;
        StringTextComponent tooltip = new StringTextComponent(tooltipModifier >= 0 ? "+" : "");
        tooltip.appendString(this.operation == OPERATION.ADDITION ? String.valueOf(tooltipModifier) : tooltipModifier + "%");
        tooltip.appendString(" ");
        tooltip.appendSibling(new TranslationTextComponent(this.requirementType.getTranslationKey()));
        if(this.requirementType != Registration.SPEED_REQUIREMENT.get()) {
            tooltip.appendString(" ");
            tooltip.appendSibling(new TranslationTextComponent(this.mode.getTranslationKey()));
        }
        return Collections.singletonList(tooltip);
    }

    public enum OPERATION {
        ADDITION,
        MULTIPLICATION;

        public static OPERATION value(String value) {
            return valueOf(value.toUpperCase(Locale.ENGLISH));
        }
    }
}

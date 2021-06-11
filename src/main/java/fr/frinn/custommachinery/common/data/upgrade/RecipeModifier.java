package fr.frinn.custommachinery.common.data.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.crafting.requirements.RequirementType;
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
                    Codecs.REQUIREMENT_TYPE_CODEC.fieldOf("requirement").forGetter(modifier -> modifier.requirementType),
                    Codec.STRING.optionalFieldOf("target", "").forGetter(modifier -> modifier.target),
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(modifier -> modifier.mode),
                    Codecs.MODIFIER_OPERATION.fieldOf("operation").forGetter(modifier -> modifier.operation),
                    Codec.DOUBLE.fieldOf("modifier").forGetter(modifier -> modifier.modifier),
                    Codec.DOUBLE.optionalFieldOf("chance", 1.0D).forGetter(modifier -> modifier.chance)
            ).apply(energyModifierInstance, RecipeModifier::new)
    );

    private RequirementType<?> requirementType;
    private String target;
    private IRequirement.MODE mode;
    private OPERATION operation;
    private double modifier;
    private double chance;

    public RecipeModifier(RequirementType<?> requirementType, String target, IRequirement.MODE mode, OPERATION operation, double modifier, double chance) {
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
        String operator = this.modifier >= 0 && this.operation == OPERATION.ADDITION ? "+" : "";
        String modifier = this.operation == OPERATION.ADDITION ? String.valueOf(this.modifier) : this.modifier * 100 + "%";
        String path = new TranslationTextComponent(this.requirementType.getTranslationKey()).getString();
        String mode = new TranslationTextComponent(this.mode.getTranslationKey()).getString();
        StringTextComponent tooltip = new StringTextComponent(operator + modifier + " " + path + " " + mode);
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

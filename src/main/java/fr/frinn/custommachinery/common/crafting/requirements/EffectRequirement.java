package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.utils.CodecLogger;
import fr.frinn.custommachinery.api.utils.RegistryCodec;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.EffectMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.common.integration.jei.RequirementDisplayInfo;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.RomanNumber;
import net.minecraft.entity.EntityType;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;

public class EffectRequirement extends AbstractTickableRequirement<EffectMachineComponent> implements IDisplayInfoRequirement<EffectMachineComponent> {

    public static final Codec<EffectRequirement> CODEC = RecordCodecBuilder.create(effectRequirementInstance ->
            effectRequirementInstance.group(
                    RegistryCodec.EFFECT.fieldOf("effect").forGetter(requirement -> requirement.effect),
                    Codec.INT.fieldOf("time").forGetter(requirement -> requirement.time),
                    Codec.INT.fieldOf("radius").forGetter(requirement -> requirement.radius),
                    CodecLogger.loggedOptional(Codec.INT,"level", 1).forGetter(requirement -> requirement.level),
                    CodecLogger.loggedOptional(Codecs.list(RegistryCodec.ENTITY_TYPE),"filter", new ArrayList<>()).forGetter(requirement -> requirement.filter),
                    CodecLogger.loggedOptional(Codec.BOOL,"finish", false).forGetter(requirement -> requirement.applyAtEnd),
                    CodecLogger.loggedOptional(Codec.BOOL,"jei", true).forGetter(requirement -> requirement.jeiVisible)
            ).apply(effectRequirementInstance, (effect, time, radius, level, filter, finish, jei) -> {
                    EffectRequirement requirement = new EffectRequirement(effect, time, level, radius, filter, finish);
                    requirement.setJeiVisible(jei);
                    return requirement;
            })
    );

    private Effect effect;
    private int time;
    private int level;
    private int radius;
    private List<EntityType<?>> filter;
    private boolean applyAtEnd;
    private boolean jeiVisible = true;

    public EffectRequirement(Effect effect, int time, int level, int radius, List<EntityType<?>> filter, boolean applyAtEnd) {
        super(MODE.OUTPUT);
        this.effect = effect;
        this.time = time;
        this.level = level;
        this.radius = radius;
        this.filter = filter;
        this.applyAtEnd = applyAtEnd;
    }

    @Override
    public RequirementType<EffectRequirement> getType() {
        return Registration.EFFECT_REQUIREMENT.get();
    }

    @Override
    public boolean test(EffectMachineComponent component, CraftingContext context) {
        return true;
    }

    @Override
    public CraftingResult processStart(EffectMachineComponent component, CraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(EffectMachineComponent component, CraftingContext context) {
        int time = (int)context.getModifiedvalue(this.time, this, "time");
        int level = (int)context.getModifiedvalue(this.level, this, "level");
        int radius = (int)context.getModifiedvalue(this.radius, this, "radius");
        if(this.applyAtEnd)
            component.applyEffect(new EffectInstance(this.effect, time, level), radius, entity -> this.filter.isEmpty() || this.filter.contains(entity.getType()));
        return CraftingResult.success();
    }

    @Override
    public CraftingResult processTick(EffectMachineComponent component, CraftingContext context) {
        int time = (int)context.getPerTickModifiedValue(this.time, this, "time");
        int level = (int)context.getPerTickModifiedValue(this.level, this, "level");
        int radius = (int)context.getPerTickModifiedValue(this.radius, this, "radius");
        if(!this.applyAtEnd)
            component.applyEffect(new EffectInstance(this.effect, time, level), radius, entity -> this.filter.isEmpty() || this.filter.contains(entity.getType()));
        return CraftingResult.success();
    }

    @Override
    public MachineComponentType<EffectMachineComponent> getComponentType() {
        return Registration.EFFECT_MACHINE_COMPONENT.get();
    }

    @Override
    public void setJeiVisible(boolean jeiVisible) {
        this.jeiVisible = jeiVisible;
    }

    @Override
    public RequirementDisplayInfo getDisplayInfo() {
        RequirementDisplayInfo info = new RequirementDisplayInfo();
        ITextComponent effect = new StringTextComponent(this.effect.getDisplayName().getString()).mergeStyle(TextFormatting.AQUA);
        ITextComponent level = new StringTextComponent(RomanNumber.toRoman(this.level)).mergeStyle(TextFormatting.GOLD);
        if(this.applyAtEnd)
            info.addTooltip(new TranslationTextComponent("custommachinery.requirements.effect.info.end", effect, level, this.time, this.radius));
        else
            info.addTooltip(new TranslationTextComponent("custommachinery.requirements.effect.info.tick", effect, level, this.time, this.radius));
        if(!this.filter.isEmpty()) {
            info.addTooltip(new TranslationTextComponent("custommachinery.requirements.effect.info.whitelist").mergeStyle(TextFormatting.AQUA));
            this.filter.forEach(type -> info.addTooltip(new StringTextComponent("* ").appendSibling(new TranslationTextComponent(type.getTranslationKey()))));
        }
        info.setVisible(this.jeiVisible);
        return info;
    }
}

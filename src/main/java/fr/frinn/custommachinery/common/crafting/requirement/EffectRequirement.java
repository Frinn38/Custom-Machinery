package fr.frinn.custommachinery.common.crafting.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.codec.CodecLogger;
import fr.frinn.custommachinery.api.codec.RegistryCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.api.requirement.ITickableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.apiimpl.requirement.AbstractRequirement;
import fr.frinn.custommachinery.common.data.component.EffectMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.RomanNumber;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;

public class EffectRequirement extends AbstractRequirement<EffectMachineComponent> implements ITickableRequirement<EffectMachineComponent>, IDisplayInfoRequirement {

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

    private final Effect effect;
    private final int time;
    private final int level;
    private final int radius;
    private final List<EntityType<?>> filter;
    private final boolean applyAtEnd;
    private boolean jeiVisible = true;

    public EffectRequirement(Effect effect, int time, int level, int radius, List<EntityType<?>> filter, boolean applyAtEnd) {
        super(RequirementIOMode.OUTPUT);
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
    public boolean test(EffectMachineComponent component, ICraftingContext context) {
        return true;
    }

    @Override
    public CraftingResult processStart(EffectMachineComponent component, ICraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(EffectMachineComponent component, ICraftingContext context) {
        if(this.applyAtEnd) {
            int time = (int)context.getModifiedValue(this.time, this, "time");
            int level = MathHelper.clamp((int)context.getModifiedValue(this.level, this, "level") - 1, 0, 255);
            int radius = (int)context.getModifiedValue(this.radius, this, "radius");
            component.applyEffect(new EffectInstance(this.effect, time, level - 1), radius, entity -> this.filter.isEmpty() || this.filter.contains(entity.getType()));
        }
        return CraftingResult.success();
    }

    @Override
    public CraftingResult processTick(EffectMachineComponent component, ICraftingContext context) {
        if(!this.applyAtEnd) {
            int time = (int)context.getPerTickModifiedValue(this.time, this, "time");
            int level = MathHelper.clamp((int)context.getPerTickModifiedValue(this.level, this, "level") - 1, 0, 255);
            int radius = (int)context.getPerTickModifiedValue(this.radius, this, "radius");
            component.applyEffect(new EffectInstance(this.effect, time, level), radius, entity -> this.filter.isEmpty() || this.filter.contains(entity.getType()));
        }
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
    public void getDisplayInfo(IDisplayInfo info) {
        ITextComponent effect = new StringTextComponent(this.effect.getDisplayName().getString()).mergeStyle(TextFormatting.AQUA);
        ITextComponent level = this.level <= 0 ? StringTextComponent.EMPTY : new StringTextComponent(RomanNumber.toRoman(this.level)).mergeStyle(TextFormatting.GOLD);
        if(this.applyAtEnd)
            info.addTooltip(new TranslationTextComponent("custommachinery.requirements.effect.info.end", effect, level, this.time, this.radius));
        else
            info.addTooltip(new TranslationTextComponent("custommachinery.requirements.effect.info.tick", effect, level, this.time, this.radius));
        if(!this.filter.isEmpty()) {
            info.addTooltip(new TranslationTextComponent("custommachinery.requirements.effect.info.whitelist").mergeStyle(TextFormatting.AQUA));
            this.filter.forEach(type -> info.addTooltip(new StringTextComponent("* ").appendSibling(new TranslationTextComponent(type.getTranslationKey()))));
        }
        info.setVisible(this.jeiVisible);
        info.setItemIcon(PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), Potions.HEALING));
    }
}

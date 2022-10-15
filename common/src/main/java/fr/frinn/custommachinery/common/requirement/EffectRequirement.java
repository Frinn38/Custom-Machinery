package fr.frinn.custommachinery.common.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.api.requirement.ITickableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.component.EffectMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.RomanNumber;
import fr.frinn.custommachinery.impl.codec.CodecLogger;
import fr.frinn.custommachinery.impl.codec.RegistrarCodec;
import fr.frinn.custommachinery.impl.requirement.AbstractDelayedChanceableRequirement;
import fr.frinn.custommachinery.impl.requirement.AbstractDelayedRequirement;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

import java.util.ArrayList;
import java.util.List;

public class EffectRequirement extends AbstractDelayedChanceableRequirement<EffectMachineComponent> implements ITickableRequirement<EffectMachineComponent>, IDisplayInfoRequirement {

    public static final Codec<EffectRequirement> CODEC = RecordCodecBuilder.create(effectRequirementInstance ->
            effectRequirementInstance.group(
                    RegistrarCodec.EFFECT.fieldOf("effect").forGetter(requirement -> requirement.effect),
                    Codec.INT.fieldOf("time").forGetter(requirement -> requirement.time),
                    Codec.INT.fieldOf("radius").forGetter(requirement -> requirement.radius),
                    CodecLogger.loggedOptional(Codec.INT,"level", 1).forGetter(requirement -> requirement.level),
                    CodecLogger.loggedOptional(Codecs.list(RegistrarCodec.ENTITY),"filter", new ArrayList<>()).forGetter(requirement -> requirement.filter),
                    CodecLogger.loggedOptional(Codec.BOOL,"finish", false).forGetter(requirement -> requirement.applyAtEnd),
                    CodecLogger.loggedOptional(Codec.doubleRange(0.0D, 1.0D), "delay", 0.0D).forGetter(AbstractDelayedRequirement::getDelay),
                    CodecLogger.loggedOptional(Codec.doubleRange(0.0D, 1.0D), "chance", 1.0D).forGetter(AbstractDelayedChanceableRequirement::getChance)
            ).apply(effectRequirementInstance, (effect, time, radius, level, filter, finish, delay, chance) -> {
                EffectRequirement requirement = new EffectRequirement(effect, time, radius, level, filter, finish);
                requirement.setDelay(delay);
                requirement.setChance(chance);
                return requirement;
            })
    );

    private final MobEffect effect;
    private final int time;
    private final int level;
    private final int radius;
    private final List<EntityType<?>> filter;
    private final boolean applyAtEnd;

    public EffectRequirement(MobEffect effect, int time, int level, int radius, List<EntityType<?>> filter, boolean applyAtEnd) {
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
        if(this.applyAtEnd && getDelay() == 0.0D) {
            int time = (int)context.getModifiedValue(this.time, this, "time");
            int level = Mth.clamp((int)context.getModifiedValue(this.level, this, "level") - 1, 0, 255);
            int radius = (int)context.getModifiedValue(this.radius, this, "radius");
            component.applyEffect(new MobEffectInstance(this.effect, time, level - 1), radius, entity -> this.filter.isEmpty() || this.filter.contains(entity.getType()));
        }
        return CraftingResult.success();
    }

    @Override
    public CraftingResult processTick(EffectMachineComponent component, ICraftingContext context) {
        if(!this.applyAtEnd && getDelay() == 0.0D) {
            int time = (int)context.getPerTickModifiedValue(this.time, this, "time");
            int level = Mth.clamp((int)context.getPerTickModifiedValue(this.level, this, "level") - 1, 0, 255);
            int radius = (int)context.getPerTickModifiedValue(this.radius, this, "radius");
            component.applyEffect(new MobEffectInstance(this.effect, time, level), radius, entity -> this.filter.isEmpty() || this.filter.contains(entity.getType()));
        }
        return CraftingResult.success();
    }

    @Override
    public CraftingResult execute(EffectMachineComponent component, ICraftingContext context) {
        int time = (int)context.getPerTickModifiedValue(this.time, this, "time");
        int level = Mth.clamp((int)context.getPerTickModifiedValue(this.level, this, "level") - 1, 0, 255);
        int radius = (int)context.getPerTickModifiedValue(this.radius, this, "radius");
        component.applyEffect(new MobEffectInstance(this.effect, time, level), radius, entity -> this.filter.isEmpty() || this.filter.contains(entity.getType()));
        return CraftingResult.success();
    }

    @Override
    public MachineComponentType<EffectMachineComponent> getComponentType() {
        return Registration.EFFECT_MACHINE_COMPONENT.get();
    }

    @Override
    public void getDisplayInfo(IDisplayInfo info) {
        Component effect = new TextComponent(this.effect.getDisplayName().getString()).withStyle(ChatFormatting.AQUA);
        Component level = this.level <= 0 ? TextComponent.EMPTY : new TextComponent(RomanNumber.toRoman(this.level)).withStyle(ChatFormatting.GOLD);
        if(this.applyAtEnd)
            info.addTooltip(new TranslatableComponent("custommachinery.requirements.effect.info.end", effect, level, this.time, this.radius));
        else
            info.addTooltip(new TranslatableComponent("custommachinery.requirements.effect.info.tick", effect, level, this.time, this.radius));
        if(!this.filter.isEmpty()) {
            info.addTooltip(new TranslatableComponent("custommachinery.requirements.effect.info.whitelist").withStyle(ChatFormatting.AQUA));
            this.filter.forEach(type -> info.addTooltip(new TextComponent("* ").append(new TranslatableComponent(type.getDescriptionId()))));
        }
        info.setItemIcon(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.HEALING));
    }
}

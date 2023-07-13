package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
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
import fr.frinn.custommachinery.common.util.RomanNumber;
import fr.frinn.custommachinery.impl.codec.RegistrarCodec;
import fr.frinn.custommachinery.impl.requirement.AbstractDelayedChanceableRequirement;
import fr.frinn.custommachinery.impl.requirement.AbstractDelayedRequirement;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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

    public static final NamedCodec<EffectRequirement> CODEC = NamedCodec.record(effectRequirementInstance ->
            effectRequirementInstance.group(
                    RegistrarCodec.EFFECT.fieldOf("effect").forGetter(requirement -> requirement.effect),
                    NamedCodec.INT.fieldOf("time").forGetter(requirement -> requirement.time),
                    NamedCodec.INT.fieldOf("radius").forGetter(requirement -> requirement.radius),
                    NamedCodec.INT.optionalFieldOf("level", 1).forGetter(requirement -> requirement.level),
                    RegistrarCodec.ENTITY.listOf().optionalFieldOf("filter", new ArrayList<>()).forGetter(requirement -> requirement.filter),
                    NamedCodec.BOOL.optionalFieldOf("finish", false).forGetter(requirement -> requirement.applyAtEnd),
                    NamedCodec.doubleRange(0.0D, 1.0D).optionalFieldOf("delay", 0.0D).forGetter(AbstractDelayedRequirement::getDelay),
                    NamedCodec.doubleRange(0.0D, 1.0D).optionalFieldOf("chance", 1.0D).forGetter(AbstractDelayedChanceableRequirement::getChance)
            ).apply(effectRequirementInstance, (effect, time, radius, level, filter, finish, delay, chance) -> {
                EffectRequirement requirement = new EffectRequirement(effect, time, radius, level, filter, finish);
                requirement.setDelay(delay);
                requirement.setChance(chance);
                return requirement;
            }), "Effect requirement"
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
            int time = (int)context.getIntegerModifiedValue(this.time, this, "time");
            int level = Mth.clamp((int)context.getIntegerModifiedValue(this.level, this, "level") - 1, 0, 255);
            int radius = (int)context.getIntegerModifiedValue(this.radius, this, "radius");
            component.applyEffect(new MobEffectInstance(this.effect, time, level - 1), radius, entity -> this.filter.isEmpty() || this.filter.contains(entity.getType()));
        }
        return CraftingResult.success();
    }

    @Override
    public CraftingResult processTick(EffectMachineComponent component, ICraftingContext context) {
        if(!this.applyAtEnd && getDelay() == 0.0D) {
            int time = (int)context.getPerTickIntegerModifiedValue(this.time, this, "time");
            int level = Mth.clamp((int)context.getPerTickIntegerModifiedValue(this.level, this, "level") - 1, 0, 255);
            int radius = (int)context.getPerTickIntegerModifiedValue(this.radius, this, "radius");
            component.applyEffect(new MobEffectInstance(this.effect, time, level), radius, entity -> this.filter.isEmpty() || this.filter.contains(entity.getType()));
        }
        return CraftingResult.success();
    }

    @Override
    public CraftingResult execute(EffectMachineComponent component, ICraftingContext context) {
        int time = (int)context.getPerTickIntegerModifiedValue(this.time, this, "time");
        int level = Mth.clamp((int)context.getPerTickIntegerModifiedValue(this.level, this, "level") - 1, 0, 255);
        int radius = (int)context.getPerTickIntegerModifiedValue(this.radius, this, "radius");
        component.applyEffect(new MobEffectInstance(this.effect, time, level), radius, entity -> this.filter.isEmpty() || this.filter.contains(entity.getType()));
        return CraftingResult.success();
    }

    @Override
    public MachineComponentType<EffectMachineComponent> getComponentType() {
        return Registration.EFFECT_MACHINE_COMPONENT.get();
    }

    @Override
    public void getDisplayInfo(IDisplayInfo info) {
        Component effect = Component.literal(this.effect.getDisplayName().getString()).withStyle(ChatFormatting.AQUA);
        Component level = this.level <= 0 ? Component.empty() : Component.literal(RomanNumber.toRoman(this.level)).withStyle(ChatFormatting.GOLD);
        if(this.applyAtEnd)
            info.addTooltip(Component.translatable("custommachinery.requirements.effect.info.end", effect, level, this.time, this.radius));
        else
            info.addTooltip(Component.translatable("custommachinery.requirements.effect.info.tick", effect, level, this.time, this.radius));
        if(!this.filter.isEmpty()) {
            info.addTooltip(Component.translatable("custommachinery.requirements.effect.info.whitelist").withStyle(ChatFormatting.AQUA));
            this.filter.forEach(type -> info.addTooltip(Component.literal("* ").append(Component.translatable(type.getDescriptionId()))));
        }
        info.setItemIcon(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.HEALING));
    }
}

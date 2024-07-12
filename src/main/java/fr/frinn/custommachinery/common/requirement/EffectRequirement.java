package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IRequirementList;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RecipeRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.component.EffectMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.RomanNumber;
import fr.frinn.custommachinery.impl.codec.RegistrarCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

import java.util.ArrayList;
import java.util.List;

public record EffectRequirement(Holder<MobEffect> effect, int time, int level, int radius, List<EntityType<?>> filter, boolean applyAtEnd) implements IRequirement<EffectMachineComponent> {

    public static final NamedCodec<EffectRequirement> CODEC = NamedCodec.record(effectRequirementInstance ->
            effectRequirementInstance.group(
                    RegistrarCodec.EFFECT.fieldOf("effect").forGetter(requirement -> requirement.effect.value()),
                    NamedCodec.INT.fieldOf("time").forGetter(requirement -> requirement.time),
                    NamedCodec.INT.fieldOf("radius").forGetter(requirement -> requirement.radius),
                    NamedCodec.INT.optionalFieldOf("level", 1).forGetter(requirement -> requirement.level),
                    RegistrarCodec.ENTITY.listOf().optionalFieldOf("filter", new ArrayList<>()).forGetter(requirement -> requirement.filter),
                    NamedCodec.BOOL.optionalFieldOf("finish", false).forGetter(requirement -> requirement.applyAtEnd)
            ).apply(effectRequirementInstance, (effect, time, radius, level, filter, finish) ->
                    new EffectRequirement(Holder.direct(effect), time, radius, level, filter, finish)), "Effect requirement"
    );

    @Override
    public RequirementType<EffectRequirement> getType() {
        return Registration.EFFECT_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType<EffectMachineComponent> getComponentType() {
        return Registration.EFFECT_MACHINE_COMPONENT.get();
    }

    @Override
    public RequirementIOMode getMode() {
        return RequirementIOMode.OUTPUT;
    }

    @Override
    public boolean test(EffectMachineComponent component, ICraftingContext context) {
        return true;
    }

    @Override
    public void gatherRequirements(IRequirementList<EffectMachineComponent> list) {
        if(this.applyAtEnd)
            list.processDelayed(1.0D, this::process);
        else
            list.processEachTick(this::processTick);
    }

    public CraftingResult process(EffectMachineComponent component, ICraftingContext context) {
        int time = (int)context.getIntegerModifiedValue(this.time, this, "time");
        int level = Mth.clamp((int)context.getIntegerModifiedValue(this.level, this, "level") - 1, 0, 255);
        int radius = (int)context.getIntegerModifiedValue(this.radius, this, "radius");
        component.applyEffect(new MobEffectInstance(this.effect, time, level), radius, entity -> this.filter.isEmpty() || this.filter.contains(entity.getType()));
        return CraftingResult.success();
    }

    public CraftingResult processTick(EffectMachineComponent component, ICraftingContext context) {
        int time = (int)context.getPerTickIntegerModifiedValue(this.time, this, "time");
        int level = Mth.clamp((int)context.getPerTickIntegerModifiedValue(this.level, this, "level") - 1, 0, 255);
        int radius = (int)context.getPerTickIntegerModifiedValue(this.radius, this, "radius");
        component.applyEffect(new MobEffectInstance(this.effect, time, level), radius, entity -> this.filter.isEmpty() || this.filter.contains(entity.getType()));
        return CraftingResult.success();
    }

    @Override
    public void getDefaultDisplayInfo(IDisplayInfo info, RecipeRequirement<?, ?> requirement) {
        Component effect = Component.literal(this.effect.value().getDisplayName().getString()).withStyle(ChatFormatting.AQUA);
        Component level = this.level <= 0 ? Component.empty() : Component.literal(RomanNumber.toRoman(this.level)).withStyle(ChatFormatting.GOLD);
        if(this.applyAtEnd)
            info.addTooltip(Component.translatable("custommachinery.requirements.effect.info.end", effect, level, this.time, this.radius));
        else
            info.addTooltip(Component.translatable("custommachinery.requirements.effect.info.tick", effect, level, this.time, this.radius));
        if(!this.filter.isEmpty()) {
            info.addTooltip(Component.translatable("custommachinery.requirements.effect.info.whitelist").withStyle(ChatFormatting.AQUA));
            this.filter.forEach(type -> info.addTooltip(Component.literal("* ").append(Component.translatable(type.getDescriptionId()))));
        }
        info.setItemIcon(PotionContents.createItemStack(Items.POTION, Potions.HEALING));
    }
}

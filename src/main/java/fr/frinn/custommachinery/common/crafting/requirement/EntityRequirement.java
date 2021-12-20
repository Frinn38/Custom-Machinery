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
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.ITickableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.apiimpl.requirement.AbstractRequirement;
import fr.frinn.custommachinery.common.data.component.EntityMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public class EntityRequirement extends AbstractRequirement<EntityMachineComponent> implements ITickableRequirement<EntityMachineComponent>, IDisplayInfoRequirement {

    public static final Codec<EntityRequirement> CODEC = RecordCodecBuilder.create(entityRequirementInstance ->
            entityRequirementInstance.group(
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(IRequirement::getMode),
                    Codecs.ENTITY_REQUIREMENT_ACTION_CODEC.fieldOf("action").forGetter(requirement -> requirement.action),
                    Codec.INT.fieldOf("amount").forGetter(requirement -> requirement.amount),
                    Codec.INT.fieldOf("radius").forGetter(requirement -> requirement.radius),
                    CodecLogger.loggedOptional(Codecs.list(RegistryCodec.ENTITY_TYPE),"filter", Collections.emptyList()).forGetter(requirement -> requirement.filter),
                    CodecLogger.loggedOptional(Codec.BOOL,"whitelist", false).forGetter(requirement -> requirement.whitelist),
                    CodecLogger.loggedOptional(Codec.BOOL,"jei", true).forGetter(requirement -> requirement.jeiVisible)
            ).apply(entityRequirementInstance, (mode, action, amount, radius, filter, whitelist, jei) -> {
                    EntityRequirement requirement = new EntityRequirement(mode, action, amount, radius, filter, whitelist);
                    requirement.setJeiVisible(jei);
                    return requirement;
            })
    );

    private final ACTION action;
    private final int amount;
    private final int radius;
    private final List<EntityType<?>> filter;
    private final boolean whitelist;
    private final Predicate<Entity> predicate;
    private boolean jeiVisible = true;

    public EntityRequirement(RequirementIOMode mode, ACTION action, int amount, int radius, List<EntityType<?>> filter, boolean whitelist) {
        super(mode);
        this.action = action;
        this.amount = amount;
        this.radius = radius;
        this.filter = filter;
        this.whitelist = whitelist;
        this.predicate = entity -> filter.contains(entity.getType()) == whitelist;
    }

    @Override
    public RequirementType<EntityRequirement> getType() {
        return Registration.ENTITY_REQUIREMENT.get();
    }

    @Override
    public boolean test(EntityMachineComponent component, ICraftingContext context) {
        int amount = (int)context.getModifiedValue(this.amount, this, null);
        int radius = (int)context.getModifiedValue(this.radius, this, "radius");
        if(this.action == ACTION.CHECK_AMOUNT || this.action == ACTION.KILL)
            return component.getEntitiesInRadius(radius, this.predicate) >= amount;
        else
            return component.getEntitiesInRadiusHealth(radius, this.predicate) >= amount;
    }

    @Override
    public CraftingResult processStart(EntityMachineComponent component, ICraftingContext context) {
        int amount = (int)context.getModifiedValue(this.amount, this, null);
        int radius = (int)context.getModifiedValue(this.radius, this, "radius");
        if(getMode() == RequirementIOMode.INPUT) {
            switch (this.action) {
                case CHECK_AMOUNT:
                    return component.getEntitiesInRadius(radius, this.predicate) >= amount ? CraftingResult.success() : CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.entity.amount.error"));
                case CHECK_HEALTH:
                    return component.getEntitiesInRadiusHealth(radius, this.predicate) >= amount ? CraftingResult.success() : CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.entity.health.error", amount));
                case CONSUME_HEALTH:
                    if(component.getEntitiesInRadiusHealth(radius, this.predicate) >= amount) {
                        component.removeEntitiesHealth(radius, this.predicate, amount);
                        return CraftingResult.success();
                    }
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.entity.health.error", amount));
                case KILL:
                    if(component.getEntitiesInRadius(radius, this.predicate) >= amount) {
                        component.killEntities(radius, this.predicate, amount);
                        return CraftingResult.success();
                    }
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.entity.amount.error"));
            }
        }
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(EntityMachineComponent component, ICraftingContext context) {
        int amount = (int)context.getModifiedValue(this.amount, this, null);
        int radius = (int)context.getModifiedValue(this.radius, this, "radius");
        if(getMode() == RequirementIOMode.OUTPUT) {
            switch (this.action) {
                case CONSUME_HEALTH:
                    if(component.getEntitiesInRadiusHealth(radius, this.predicate) >= amount) {
                        component.removeEntitiesHealth(radius, this.predicate, amount);
                        return CraftingResult.success();
                    }
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.entity.health.error", amount));
                case KILL:
                    if(component.getEntitiesInRadius(radius, this.predicate) >= amount) {
                        component.killEntities(radius, this.predicate, amount);
                        return CraftingResult.success();
                    }
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.entity.amount.error"));
            }
        }
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<EntityMachineComponent> getComponentType() {
        return Registration.ENTITY_MACHINE_COMPONENT.get();
    }

    @Override
    public CraftingResult processTick(EntityMachineComponent component, ICraftingContext context) {
        int amount = (int)context.getModifiedValue(this.amount, this, null);
        int radius = (int)context.getModifiedValue(this.radius, this, "radius");
        if(this.action == ACTION.CHECK_AMOUNT)
            return component.getEntitiesInRadius(radius, this.predicate) >= amount ? CraftingResult.success() : CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.entity.amount.error"));
        else if(this.action == ACTION.CHECK_HEALTH)
            return component.getEntitiesInRadiusHealth(radius, this.predicate) >= amount ? CraftingResult.success() : CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.entity.health.error", amount));
        else
            return CraftingResult.pass();
    }

    @Override
    public void setJeiVisible(boolean jeiVisible) {
        this.jeiVisible = jeiVisible;
    }

    @Override
    public void getDisplayInfo(IDisplayInfo info) {
        info.setVisible(this.jeiVisible);
        info.addTooltip(new TranslationTextComponent("custommachinery.requirements.entity." + this.action.toString().toLowerCase(Locale.ENGLISH) + ".info", this.amount, this.radius));
        if(!this.filter.isEmpty()) {
            if(this.whitelist)
                info.addTooltip(new TranslationTextComponent("custommachinery.requirements.entity.whitelist"));
            else
                info.addTooltip(new TranslationTextComponent("custommachinery.requirements.entity.blacklist"));
        }
        this.filter.forEach(type -> info.addTooltip(new StringTextComponent("*").appendSibling(type.getName())));
    }

    public enum ACTION {
        CHECK_AMOUNT,
        CHECK_HEALTH,
        CONSUME_HEALTH,
        KILL;

        public static ACTION value(String mode) {
            return valueOf(mode.toUpperCase(Locale.ENGLISH));
        }
    }
}

package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.components.MachineComponentType;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.EntityMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.common.integration.jei.RequirementDisplayInfo;
import fr.frinn.custommachinery.common.util.Codecs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public class EntityRequirement extends AbstractTickableRequirement<EntityMachineComponent> implements IDisplayInfoRequirement<EntityMachineComponent> {

    public static final Codec<EntityRequirement> CODEC = RecordCodecBuilder.create(entityRequirementInstance ->
            entityRequirementInstance.group(
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(AbstractTickableRequirement::getMode),
                    Codecs.ENTITY_REQUIREMENT_ACTION_CODEC.fieldOf("action").forGetter(requirement -> requirement.action),
                    Codec.INT.fieldOf("amount").forGetter(requirement -> requirement.amount),
                    Codec.INT.fieldOf("radius").forGetter(requirement -> requirement.radius),
                    Codecs.ENTITY_TYPE_CODEC.listOf().optionalFieldOf("filter", new ArrayList<>()).forGetter(requirement -> requirement.filter),
                    Codec.BOOL.optionalFieldOf("whitelist", false).forGetter(requirement -> requirement.whitelist),
                    Codec.BOOL.optionalFieldOf("jei", true).forGetter(requirement -> requirement.jeiVisible)
            ).apply(entityRequirementInstance, EntityRequirement::new)
    );

    private ACTION action;
    private int amount;
    private int radius;
    private List<EntityType<?>> filter;
    private boolean whitelist;
    private Predicate<Entity> predicate;
    private boolean jeiVisible;

    public EntityRequirement(MODE mode, ACTION action, int amount, int radius, List<EntityType<?>> filter, boolean whitelist, boolean jeiVisible) {
        super(mode);
        this.action = action;
        this.amount = amount;
        this.radius = radius;
        this.filter = filter;
        this.whitelist = whitelist;
        this.predicate = entity -> filter.contains(entity.getType()) == whitelist;
        this.jeiVisible = jeiVisible;
    }

    @Override
    public RequirementType<EntityRequirement> getType() {
        return Registration.ENTITY_REQUIREMENT.get();
    }

    @Override
    public boolean test(EntityMachineComponent component, CraftingContext context) {
        int amount = (int)context.getModifiedvalue(this.amount, this, null);
        int radius = (int)context.getModifiedvalue(this.radius, this, "radius");
        if(this.action == ACTION.CHECK_AMOUNT || this.action == ACTION.KILL)
            return component.getEntitiesInRadius(radius, this.predicate) >= amount;
        else
            return component.getEntitiesInRadiusHealth(radius, this.predicate) >= amount;
    }

    @Override
    public CraftingResult processStart(EntityMachineComponent component, CraftingContext context) {
        int amount = (int)context.getModifiedvalue(this.amount, this, null);
        int radius = (int)context.getModifiedvalue(this.radius, this, "radius");
        if(getMode() == MODE.INPUT) {
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
    public CraftingResult processEnd(EntityMachineComponent component, CraftingContext context) {
        int amount = (int)context.getModifiedvalue(this.amount, this, null);
        int radius = (int)context.getModifiedvalue(this.radius, this, "radius");
        if(getMode() == MODE.OUTPUT) {
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
    public CraftingResult processTick(EntityMachineComponent component, CraftingContext context) {
        int amount = (int)context.getModifiedvalue(this.amount, this, null);
        int radius = (int)context.getModifiedvalue(this.radius, this, "radius");
        if(this.action == ACTION.CHECK_AMOUNT)
            return component.getEntitiesInRadius(radius, this.predicate) >= amount ? CraftingResult.success() : CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.entity.amount.error"));
        else if(this.action == ACTION.CHECK_HEALTH)
            return component.getEntitiesInRadiusHealth(radius, this.predicate) >= amount ? CraftingResult.success() : CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.entity.health.error", amount));
        else
            return CraftingResult.pass();
    }

    @Override
    public RequirementDisplayInfo getDisplayInfo() {
        RequirementDisplayInfo info = new RequirementDisplayInfo();
        info.setVisible(this.jeiVisible);
        info.addTooltip(new TranslationTextComponent("custommachinery.requirements.entity." + this.action.toString().toLowerCase(Locale.ENGLISH) + ".info", this.amount, this.radius));
        if(!this.filter.isEmpty()) {
            if(this.whitelist)
                info.addTooltip(new TranslationTextComponent("custommachinery.requirements.entity.whitelist"));
            else
                info.addTooltip(new TranslationTextComponent("custommachinery.requirements.entity.blacklist"));
        }
        this.filter.forEach(type -> info.addTooltip(new StringTextComponent("*").appendSibling(type.getName())));
        return info;
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

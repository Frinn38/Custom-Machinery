package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.ITickableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.component.EntityMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.codec.RegistrarCodec;
import fr.frinn.custommachinery.impl.requirement.AbstractDelayedChanceableRequirement;
import fr.frinn.custommachinery.impl.requirement.AbstractDelayedRequirement;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public class EntityRequirement extends AbstractDelayedChanceableRequirement<EntityMachineComponent> implements ITickableRequirement<EntityMachineComponent>, IDisplayInfoRequirement {

    public static final NamedCodec<EntityRequirement> CODEC = NamedCodec.record(entityRequirementInstance ->
            entityRequirementInstance.group(
                    RequirementIOMode.CODEC.fieldOf("mode").forGetter(IRequirement::getMode),
                    ACTION.CODEC.fieldOf("action").forGetter(requirement -> requirement.action),
                    NamedCodec.INT.fieldOf("amount").forGetter(requirement -> requirement.amount),
                    NamedCodec.INT.fieldOf("radius").forGetter(requirement -> requirement.radius),
                    RegistrarCodec.ENTITY.listOf().optionalFieldOf("filter", Collections.emptyList()).forGetter(requirement -> requirement.filter),
                    NamedCodec.BOOL.optionalFieldOf("whitelist", false).forGetter(requirement -> requirement.whitelist),
                    NamedCodec.doubleRange(0.0D, 1.0D).optionalFieldOf("delay", 0.0D).forGetter(AbstractDelayedRequirement::getDelay),
                    NamedCodec.doubleRange(0.0D, 1.0D).optionalFieldOf("chance", 1.0D).forGetter(AbstractDelayedChanceableRequirement::getChance)
            ).apply(entityRequirementInstance, (mode, action, amount, radius, filter, whitelist, delay, chance) -> {
                EntityRequirement requirement = new EntityRequirement(mode, action, amount, radius, filter, whitelist);
                requirement.setDelay(delay);
                requirement.setChance(chance);
                return requirement;
            }), "Entity requirement"
    );

    private final ACTION action;
    private final int amount;
    private final int radius;
    private final List<EntityType<?>> filter;
    private final boolean whitelist;
    private final Predicate<Entity> predicate;

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
        if(getDelay() != 0.0D)
            return CraftingResult.pass();
        int amount = (int)context.getModifiedValue(this.amount, this, null);
        int radius = (int)context.getModifiedValue(this.radius, this, "radius");
        if(getMode() == RequirementIOMode.INPUT) {
            switch (this.action) {
                case CHECK_AMOUNT:
                    return component.getEntitiesInRadius(radius, this.predicate) >= amount ? CraftingResult.success() : CraftingResult.error(new TranslatableComponent("custommachinery.requirements.entity.amount.error"));
                case CHECK_HEALTH:
                    return component.getEntitiesInRadiusHealth(radius, this.predicate) >= amount ? CraftingResult.success() : CraftingResult.error(new TranslatableComponent("custommachinery.requirements.entity.health.error", amount));
                case CONSUME_HEALTH:
                    if(component.getEntitiesInRadiusHealth(radius, this.predicate) >= amount) {
                        component.removeEntitiesHealth(radius, this.predicate, amount);
                        return CraftingResult.success();
                    }
                    return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.entity.health.error", amount));
                case KILL:
                    if(component.getEntitiesInRadius(radius, this.predicate) >= amount) {
                        component.killEntities(radius, this.predicate, amount);
                        return CraftingResult.success();
                    }
                    return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.entity.amount.error"));
            }
        }
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(EntityMachineComponent component, ICraftingContext context) {
        if(getDelay() != 0.0D)
            return CraftingResult.pass();
        int amount = (int)context.getModifiedValue(this.amount, this, null);
        int radius = (int)context.getModifiedValue(this.radius, this, "radius");
        if(getMode() == RequirementIOMode.OUTPUT) {
            switch (this.action) {
                case CONSUME_HEALTH -> {
                    if (component.getEntitiesInRadiusHealth(radius, this.predicate) >= amount) {
                        component.removeEntitiesHealth(radius, this.predicate, amount);
                        return CraftingResult.success();
                    }
                    return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.entity.health.error", amount));
                }
                case KILL -> {
                    if (component.getEntitiesInRadius(radius, this.predicate) >= amount) {
                        component.killEntities(radius, this.predicate, amount);
                        return CraftingResult.success();
                    }
                    return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.entity.amount.error"));
                }
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
            return component.getEntitiesInRadius(radius, this.predicate) >= amount ? CraftingResult.success() : CraftingResult.error(new TranslatableComponent("custommachinery.requirements.entity.amount.error"));
        else if(this.action == ACTION.CHECK_HEALTH)
            return component.getEntitiesInRadiusHealth(radius, this.predicate) >= amount ? CraftingResult.success() : CraftingResult.error(new TranslatableComponent("custommachinery.requirements.entity.health.error", amount));
        else
            return CraftingResult.pass();
    }

    @Override
    public CraftingResult execute(EntityMachineComponent component, ICraftingContext context) {
        switch (this.action) {
            case CONSUME_HEALTH -> {
                if (component.getEntitiesInRadiusHealth(radius, this.predicate) >= amount) {
                    component.removeEntitiesHealth(radius, this.predicate, amount);
                    return CraftingResult.success();
                }
                return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.entity.health.error", amount));
            }
            case KILL -> {
                if (component.getEntitiesInRadius(radius, this.predicate) >= amount) {
                    component.killEntities(radius, this.predicate, amount);
                    return CraftingResult.success();
                }
                return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.entity.amount.error"));
            }
        }
        return CraftingResult.pass();
    }

    @Override
    public void getDisplayInfo(IDisplayInfo info) {
        info.addTooltip(new TranslatableComponent("custommachinery.requirements.entity." + this.action.toString().toLowerCase(Locale.ENGLISH) + ".info", this.amount, this.radius));
        if(!this.filter.isEmpty()) {
            if(this.whitelist)
                info.addTooltip(new TranslatableComponent("custommachinery.requirements.entity.whitelist"));
            else
                info.addTooltip(new TranslatableComponent("custommachinery.requirements.entity.blacklist"));
        }
        this.filter.forEach(type -> info.addTooltip(new TextComponent("*").append(type.getDescription())));
        info.setItemIcon(Items.COW_SPAWN_EGG);
    }

    public enum ACTION {
        CHECK_AMOUNT,
        CHECK_HEALTH,
        CONSUME_HEALTH,
        KILL;

        public static final NamedCodec<ACTION> CODEC = NamedCodec.enumCodec(ACTION.class);

        public static ACTION value(String mode) {
            return valueOf(mode.toUpperCase(Locale.ENGLISH));
        }
    }
}

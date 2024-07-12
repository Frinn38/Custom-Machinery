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
import fr.frinn.custommachinery.common.component.EntityMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.codec.RegistrarCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public record EntityRequirement(RequirementIOMode mode, ACTION action, int amount, int radius, List<EntityType<?>> filter, boolean whitelist) implements IRequirement<EntityMachineComponent> {

    public static final NamedCodec<EntityRequirement> CODEC = NamedCodec.record(entityRequirementInstance ->
            entityRequirementInstance.group(
                    RequirementIOMode.CODEC.fieldOf("mode").forGetter(IRequirement::getMode),
                    ACTION.CODEC.fieldOf("action").forGetter(requirement -> requirement.action),
                    NamedCodec.INT.fieldOf("amount").forGetter(requirement -> requirement.amount),
                    NamedCodec.INT.fieldOf("radius").forGetter(requirement -> requirement.radius),
                    RegistrarCodec.ENTITY.listOf().optionalFieldOf("filter", Collections.emptyList()).forGetter(requirement -> requirement.filter),
                    NamedCodec.BOOL.optionalFieldOf("whitelist", false).forGetter(requirement -> requirement.whitelist)
            ).apply(entityRequirementInstance, EntityRequirement::new), "Entity requirement"
    );

    @Override
    public RequirementType<EntityRequirement> getType() {
        return Registration.ENTITY_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType<EntityMachineComponent> getComponentType() {
        return Registration.ENTITY_MACHINE_COMPONENT.get();
    }

    @Override
    public RequirementIOMode getMode() {
        return this.mode;
    }

    @Override
    public boolean test(EntityMachineComponent component, ICraftingContext context) {
        int amount = (int)context.getIntegerModifiedValue(this.amount, this, null);
        int radius = (int)context.getIntegerModifiedValue(this.radius, this, "radius");
        if(this.action == ACTION.CHECK_AMOUNT || this.action == ACTION.KILL)
            return component.getEntitiesInRadius(radius, this::predicate) >= amount;
        else
            return component.getEntitiesInRadiusHealth(radius, this::predicate) >= amount;
    }

    @Override
    public void gatherRequirements(IRequirementList<EntityMachineComponent> list) {
        if(this.action == ACTION.CHECK_AMOUNT || this.action == ACTION.CHECK_HEALTH)
            list.processEachTick(this::check);
        else
            list.process(this.mode, this::process);
    }

    private CraftingResult check(EntityMachineComponent component, ICraftingContext context) {
        int amount = (int)context.getIntegerModifiedValue(this.amount, this, null);
        int radius = (int)context.getIntegerModifiedValue(this.radius, this, "radius");
        if(this.action == ACTION.CHECK_AMOUNT)
            return component.getEntitiesInRadius(radius, this::predicate) >= amount ? CraftingResult.success() : CraftingResult.error(Component.translatable("custommachinery.requirements.entity.amount.error"));
        else if(this.action == ACTION.CHECK_HEALTH)
            return component.getEntitiesInRadiusHealth(radius, this::predicate) >= amount ? CraftingResult.success() : CraftingResult.error(Component.translatable("custommachinery.requirements.entity.health.error", amount));
        else
            return CraftingResult.pass();
    }

    private CraftingResult process(EntityMachineComponent component, ICraftingContext context) {
        int amount = (int)context.getIntegerModifiedValue(this.amount, this, null);
        int radius = (int)context.getIntegerModifiedValue(this.radius, this, "radius");
        switch (this.action) {
            case CONSUME_HEALTH -> {
                if (component.getEntitiesInRadiusHealth(radius, this::predicate) >= amount) {
                    component.removeEntitiesHealth(radius, this::predicate, amount);
                    return CraftingResult.success();
                }
                return CraftingResult.error(Component.translatable("custommachinery.requirements.entity.health.error", amount));
            }
            case KILL -> {
                if (component.getEntitiesInRadius(radius, this::predicate) >= amount) {
                    component.killEntities(radius, this::predicate, amount);
                    return CraftingResult.success();
                }
                return CraftingResult.error(Component.translatable("custommachinery.requirements.entity.amount.error"));
            }
        }
        return CraftingResult.pass();
    }

    private boolean predicate(Entity entity) {
        return this.filter.contains(entity.getType()) == this.whitelist;
    }

    @Override
    public void getDefaultDisplayInfo(IDisplayInfo info, RecipeRequirement<?, ?> requirement) {
        info.addTooltip(Component.translatable("custommachinery.requirements.entity." + this.action.toString().toLowerCase(Locale.ENGLISH) + ".info", this.amount, this.radius));
        if(!this.filter.isEmpty()) {
            if(this.whitelist)
                info.addTooltip(Component.translatable("custommachinery.requirements.entity.whitelist"));
            else
                info.addTooltip(Component.translatable("custommachinery.requirements.entity.blacklist"));
        }
        this.filter.forEach(type -> info.addTooltip(Component.literal("*").append(type.getDescription())));
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

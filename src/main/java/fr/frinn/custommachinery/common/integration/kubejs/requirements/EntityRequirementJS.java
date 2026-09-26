package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.requirement.EntityRequirement;
import net.minecraft.world.entity.EntityType;

import java.util.List;

public interface EntityRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireEntities(int amount, int radius, List<EntityType<?>> filter, boolean whitelist) {
        if(!filter.isEmpty() || !whitelist)
            return this.addRequirement(new EntityRequirement(RequirementIOMode.INPUT, EntityRequirement.ACTION.CHECK_AMOUNT, amount, radius, filter, whitelist));
        return error("Can't use \"requireEntities\" in whitelist mode with an empty filter");
    }

    default RecipeJSBuilder requireEntitiesHealth(int amount, int radius, List<EntityType<?>> filter, boolean whitelist) {
        if(!filter.isEmpty() || !whitelist)
            return this.addRequirement(new EntityRequirement(RequirementIOMode.INPUT, EntityRequirement.ACTION.CHECK_HEALTH, amount, radius, filter, whitelist));
        return error("Can't use \"requireEntitiesHealth\" in whitelist mode with an empty filter");
    }

    default RecipeJSBuilder consumeEntityHealthOnStart(int amount, int radius, List<EntityType<?>> filter, boolean whitelist) {
        if(!filter.isEmpty() || !whitelist)
            return this.addRequirement(new EntityRequirement(RequirementIOMode.INPUT, EntityRequirement.ACTION.CONSUME_HEALTH, amount, radius, filter, whitelist));
        return error("Can't use \"consumeEntityHealthOnStart\" in whitelist mode with an empty filter");
    }

    default RecipeJSBuilder consumeEntityHealthOnEnd(int amount, int radius, List<EntityType<?>> filter, boolean whitelist) {
        if(!filter.isEmpty() || !whitelist)
            return this.addRequirement(new EntityRequirement(RequirementIOMode.OUTPUT, EntityRequirement.ACTION.CONSUME_HEALTH, amount, radius, filter, whitelist));
        return error("Can't use \"consumeEntityHealthOnEnd\" in whitelist mode with an empty filter");
    }

    default RecipeJSBuilder killEntitiesOnStart(int amount, int radius, List<EntityType<?>> filter, boolean whitelist) {
        if(!filter.isEmpty() || !whitelist)
            return this.addRequirement(new EntityRequirement(RequirementIOMode.INPUT, EntityRequirement.ACTION.KILL, amount, radius, filter, whitelist));
        return error("Can't use \"killEntitiesOnStart\" in whitelist mode with an empty filter");
    }

    default RecipeJSBuilder killEntitiesOnEnd(int amount, int radius, List<EntityType<?>> filter, boolean whitelist) {
        if(!filter.isEmpty() || !whitelist)
            return this.addRequirement(new EntityRequirement(RequirementIOMode.OUTPUT, EntityRequirement.ACTION.KILL, amount, radius, filter, whitelist));
        return error("Can't use \"killEntitiesOnEnd\" in whitelist mode with an empty filter");
    }
}

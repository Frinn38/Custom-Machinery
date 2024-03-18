package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.requirement.EntityRequirement;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public interface EntityRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireEntities(int amount, int radius, String[] filter, boolean whitelist) {
        List<EntityType<?>> entityFilter = new ArrayList<>();
        for(String type : filter) {
            if(Utils.isResourceNameValid(type) && BuiltInRegistries.ENTITY_TYPE.containsKey(new ResourceLocation(type)))
                entityFilter.add(BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(type)));
            else
                return error("Invalid entity ID: {}", type);
        }
        if(!entityFilter.isEmpty() || !whitelist)
            return this.addRequirement(new EntityRequirement(RequirementIOMode.INPUT, EntityRequirement.ACTION.CHECK_AMOUNT, amount, radius, entityFilter, whitelist));
        return error("Can't use \"requireEntities\" in whitelist mode with an empty filter");
    }

    default RecipeJSBuilder requireEntitiesHealth(int amount, int radius, String[] filter, boolean whitelist) {
        List<EntityType<?>> entityFilter = new ArrayList<>();
        for(String type : filter) {
            if(Utils.isResourceNameValid(type) && BuiltInRegistries.ENTITY_TYPE.containsKey(new ResourceLocation(type)))
                entityFilter.add(BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(type)));
            else
                return error("Invalid entity ID: {}", type);
        }
        if(!entityFilter.isEmpty())
            return this.addRequirement(new EntityRequirement(RequirementIOMode.INPUT, EntityRequirement.ACTION.CHECK_HEALTH, amount, radius, entityFilter, whitelist));
        return error("Can't use \"requireEntitiesHealth\" in whitelist mode with an empty filter");
    }

    default RecipeJSBuilder consumeEntityHealthOnStart(int amount, int radius, String[] filter, boolean whitelist) {
        List<EntityType<?>> entityFilter = new ArrayList<>();
        for(String type : filter) {
            if(Utils.isResourceNameValid(type) && BuiltInRegistries.ENTITY_TYPE.containsKey(new ResourceLocation(type)))
                entityFilter.add(BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(type)));
            else
                return error("Invalid entity ID: {}", type);
        }
        if(!entityFilter.isEmpty())
            return this.addRequirement(new EntityRequirement(RequirementIOMode.INPUT, EntityRequirement.ACTION.CONSUME_HEALTH, amount, radius, entityFilter, whitelist));
        return error("Can't use \"consumeEntityHealthOnStart\" in whitelist mode with an empty filter");
    }

    default RecipeJSBuilder consumeEntityHealthOnEnd(int amount, int radius, String[] filter, boolean whitelist) {
        List<EntityType<?>> entityFilter = new ArrayList<>();
        for(String type : filter) {
            if(Utils.isResourceNameValid(type) && BuiltInRegistries.ENTITY_TYPE.containsKey(new ResourceLocation(type)))
                entityFilter.add(BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(type)));
            else
                return error("Invalid entity ID: {}", type);
        }
        if(!entityFilter.isEmpty())
            return this.addRequirement(new EntityRequirement(RequirementIOMode.OUTPUT, EntityRequirement.ACTION.CONSUME_HEALTH, amount, radius, entityFilter, whitelist));
        return error("Can't use \"consumeEntityHealthOnEnd\" in whitelist mode with an empty filter");
    }

    default RecipeJSBuilder killEntitiesOnStart(int amount, int radius, String[] filter, boolean whitelist) {
        List<EntityType<?>> entityFilter = new ArrayList<>();
        for(String type : filter) {
            if(Utils.isResourceNameValid(type) && BuiltInRegistries.ENTITY_TYPE.containsKey(new ResourceLocation(type)))
                entityFilter.add(BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(type)));
            else
                return error("Invalid entity ID: {}", type);
        }
        if(!entityFilter.isEmpty())
            return this.addRequirement(new EntityRequirement(RequirementIOMode.INPUT, EntityRequirement.ACTION.KILL, amount, radius, entityFilter, whitelist));
        return error("Can't use \"killEntitiesOnStart\" in whitelist mode with an empty filter");
    }

    default RecipeJSBuilder killEntitiesOnEnd(int amount, int radius, String[] filter, boolean whitelist) {
        List<EntityType<?>> entityFilter = new ArrayList<>();
        for(String type : filter) {
            if(Utils.isResourceNameValid(type) && BuiltInRegistries.ENTITY_TYPE.containsKey(new ResourceLocation(type)))
                entityFilter.add(BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(type)));
            else
                return error("Invalid entity ID: {}", type);
        }
        if(!entityFilter.isEmpty())
            return this.addRequirement(new EntityRequirement(RequirementIOMode.OUTPUT, EntityRequirement.ACTION.KILL, amount, radius, entityFilter, whitelist));
        return error("Can't use \"killEntitiesOnEnd\" in whitelist mode with an empty filter");
    }
}

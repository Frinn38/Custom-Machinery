package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import dev.latvian.mods.kubejs.script.ScriptType;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.EntityRequirement;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface EntityRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireEntities(int amount, int radius, String[] filter, boolean whitelist) {
        List<EntityType<?>> entityFilter = Arrays.stream(filter).filter(type -> {
            if(Utils.isResourceNameValid(type) && Registry.ENTITY_TYPE.containsKey(new ResourceLocation(type)))
                return true;
            ScriptType.SERVER.console.warn("Invalid entity ID: " + type);
            return false;
        }).map(ResourceLocation::new).map(Registry.ENTITY_TYPE::get).collect(Collectors.toList());
        if(!entityFilter.isEmpty() || !whitelist)
            return this.addRequirement(new EntityRequirement(RequirementIOMode.INPUT, EntityRequirement.ACTION.CHECK_AMOUNT, amount, radius, entityFilter, whitelist));
        return this;
    }

    default RecipeJSBuilder requireEntitiesHealth(int amount, int radius, String[] filter, boolean whitelist) {
        List<EntityType<?>> entityFilter = Arrays.stream(filter).filter(type -> {
            if(Utils.isResourceNameValid(type) && Registry.ENTITY_TYPE.containsKey(new ResourceLocation(type)))
                return true;
            ScriptType.SERVER.console.warn("Invalid entity ID: " + type);
            return false;
        }).map(ResourceLocation::new).map(Registry.ENTITY_TYPE::get).collect(Collectors.toList());
        if(!entityFilter.isEmpty())
            return this.addRequirement(new EntityRequirement(RequirementIOMode.INPUT, EntityRequirement.ACTION.CHECK_HEALTH, amount, radius, entityFilter, whitelist));
        return this;
    }

    default RecipeJSBuilder consumeEntityHealthOnStart(int amount, int radius, String[] filter, boolean whitelist) {
        List<EntityType<?>> entityFilter = Arrays.stream(filter).filter(type -> {
            if(Utils.isResourceNameValid(type) && Registry.ENTITY_TYPE.containsKey(new ResourceLocation(type)))
                return true;
            ScriptType.SERVER.console.warn("Invalid entity ID: " + type);
            return false;
        }).map(ResourceLocation::new).map(Registry.ENTITY_TYPE::get).collect(Collectors.toList());
        if(!entityFilter.isEmpty())
            return this.addRequirement(new EntityRequirement(RequirementIOMode.INPUT, EntityRequirement.ACTION.CONSUME_HEALTH, amount, radius, entityFilter, whitelist));
        return this;
    }

    default RecipeJSBuilder consumeEntityHealthOnEnd(int amount, int radius, String[] filter, boolean whitelist) {
        List<EntityType<?>> entityFilter = Arrays.stream(filter).filter(type -> {
            if(Utils.isResourceNameValid(type) && Registry.ENTITY_TYPE.containsKey(new ResourceLocation(type)))
                return true;
            ScriptType.SERVER.console.warn("Invalid entity ID: " + type);
            return false;
        }).map(ResourceLocation::new).map(Registry.ENTITY_TYPE::get).collect(Collectors.toList());
        if(!entityFilter.isEmpty())
            return this.addRequirement(new EntityRequirement(RequirementIOMode.OUTPUT, EntityRequirement.ACTION.CONSUME_HEALTH, amount, radius, entityFilter, whitelist));
        return this;
    }

    default RecipeJSBuilder killEntitiesOnStart(int amount, int radius, String[] filter, boolean whitelist) {
        List<EntityType<?>> entityFilter = Arrays.stream(filter).filter(type -> {
            if(Utils.isResourceNameValid(type) && Registry.ENTITY_TYPE.containsKey(new ResourceLocation(type)))
                return true;
            ScriptType.SERVER.console.warn("Invalid entity ID: " + type);
            return false;
        }).map(ResourceLocation::new).map(Registry.ENTITY_TYPE::get).collect(Collectors.toList());
        if(!entityFilter.isEmpty())
            return this.addRequirement(new EntityRequirement(RequirementIOMode.INPUT, EntityRequirement.ACTION.KILL, amount, radius, entityFilter, whitelist));
        return this;
    }

    default RecipeJSBuilder killEntitiesOnEnd(int amount, int radius, String[] filter, boolean whitelist) {
        List<EntityType<?>> entityFilter = Arrays.stream(filter).filter(type -> {
            if(Utils.isResourceNameValid(type) && Registry.ENTITY_TYPE.containsKey(new ResourceLocation(type)))
                return true;
            ScriptType.SERVER.console.warn("Invalid entity ID: " + type);
            return false;
        }).map(ResourceLocation::new).map(Registry.ENTITY_TYPE::get).collect(Collectors.toList());
        if(!entityFilter.isEmpty())
            return this.addRequirement(new EntityRequirement(RequirementIOMode.OUTPUT, EntityRequirement.ACTION.KILL, amount, radius, entityFilter, whitelist));
        return this;
    }
}

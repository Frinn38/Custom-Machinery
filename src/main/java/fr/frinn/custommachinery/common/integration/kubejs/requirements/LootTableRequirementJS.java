package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.LootTableRequirement;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

public interface LootTableRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder lootTableOutput(String lootTable) {
        return this.lootTableOutput(lootTable, 0.0F);
    }

    default RecipeJSBuilder lootTableOutput(String lootTable, float luck) {
        try {
            return addRequirement(new LootTableRequirement(ResourceLocation.parse(lootTable), luck));
        } catch (ResourceLocationException e) {
            return error("Invalid loot table id: {}", lootTable);
        }
    }
}

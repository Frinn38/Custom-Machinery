package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.LootTableRequirement;
import net.minecraft.resources.ResourceLocation;

public interface LootTableRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder lootTableOutput(String lootTable) {
        return this.lootTableOutput(lootTable, 0.0F);
    }

    default RecipeJSBuilder lootTableOutput(String lootTable, float luck) {
        if (!ResourceLocation.isValidResourceLocation(lootTable))
            return error("Invalid loot table id: {}", lootTable);

        ResourceLocation tableLoc = new ResourceLocation(lootTable);
        return addRequirement(new LootTableRequirement(tableLoc, luck));
    }
}

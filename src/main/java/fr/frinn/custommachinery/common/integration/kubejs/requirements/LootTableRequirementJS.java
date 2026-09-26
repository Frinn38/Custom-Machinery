package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.LootTableRequirement;
import net.minecraft.resources.Identifier;

public interface LootTableRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder lootTableOutput(Identifier lootTable) {
        return this.lootTableOutput(lootTable, 0.0F);
    }

    default RecipeJSBuilder lootTableOutput(Identifier lootTable, float luck) {
        return addRequirement(new LootTableRequirement(lootTable, luck));
    }
}

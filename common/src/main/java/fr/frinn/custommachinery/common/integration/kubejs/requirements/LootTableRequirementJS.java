package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import dev.latvian.mods.kubejs.script.ScriptType;
import fr.frinn.custommachinery.common.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.LootTableRequirement;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.resources.ResourceLocation;

public interface LootTableRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder lootTableOutput(String lootTable) {
        return this.lootTableOutput(lootTable, 0.0F);
    }

    default RecipeJSBuilder lootTableOutput(String lootTable, float luck) {
        if (!Utils.isResourceNameValid(lootTable)) {
            ScriptType.SERVER.console.warn("Invalid loot table id: " + lootTable);
            return this;
        }
        ResourceLocation tableLoc = new ResourceLocation(lootTable);
        return addRequirement(new LootTableRequirement(tableLoc, luck));
    }
}

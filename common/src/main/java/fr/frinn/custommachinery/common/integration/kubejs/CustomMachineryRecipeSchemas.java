package fr.frinn.custommachinery.common.integration.kubejs;

import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.component.TimeComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.resources.ResourceLocation;

public interface CustomMachineryRecipeSchemas {

    RecipeComponent<String> RESOURCE_LOCATION = new StringComponent("machine", ResourceLocation::isValidResourceLocation);

    RecipeKey<String> MACHINE_ID = RESOURCE_LOCATION.key("machine");
    RecipeKey<Long> TIME = TimeComponent.TICKS.key("time");
    RecipeKey<OutputItem> OUTPUT = ItemComponents.OUTPUT.key("output");

    RecipeSchema CUSTOM_MACHINE = new RecipeSchema(CustomMachineRecipeBuilderJS.class, CustomMachineRecipeBuilderJS::new, MACHINE_ID, TIME);
    RecipeSchema CUSTOM_CRAFT = new RecipeSchema(CustomCraftRecipeJSBuilder.class, CustomCraftRecipeJSBuilder::new, MACHINE_ID, OUTPUT);
}

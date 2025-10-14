package fr.frinn.custommachinery.common.integration.kubejs;

import com.mojang.serialization.MapCodec;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentValue;
import dev.latvian.mods.kubejs.recipe.component.RecipeValidationContext;
import dev.latvian.mods.kubejs.recipe.schema.postprocessing.RecipePostProcessor;
import dev.latvian.mods.kubejs.recipe.schema.postprocessing.RecipePostProcessorType;
import fr.frinn.custommachinery.CustomMachinery;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class RecipeIdPostProcessor implements RecipePostProcessor {

    public static final RecipePostProcessorType<RecipeIdPostProcessor> TYPE = new RecipePostProcessorType<>(CustomMachinery.rl("recipe_id"), context -> MapCodec.unit(new RecipeIdPostProcessor()));
    public static final Map<ResourceLocation, Map<ResourceLocation, Integer>> IDS = new HashMap<>();

    @Override
    public RecipePostProcessorType<RecipeIdPostProcessor> type() {
        return TYPE;
    }

    @Override
    public void process(RecipeValidationContext ctx, KubeRecipe recipe) {
        if(!recipe.newRecipe || !(recipe instanceof AbstractRecipeJSBuilder<?> builder))
            return;

        for(RecipeComponentValue<?> value : recipe.getRecipeComponentValues()) {
            if(value.key.name.equals("machine") && value.value instanceof ResourceLocation machine) {
                int uniqueID = IDS.computeIfAbsent(builder.typeID, id -> new HashMap<>()).computeIfAbsent(machine, m -> 0);
                IDS.get(builder.typeID).put(machine, uniqueID + 1);
                recipe.id = ResourceLocation.fromNamespaceAndPath("kubejs", builder.typeID.getPath() + "/" + machine.getNamespace() + "/" + machine.getPath() + "/" + uniqueID);
            }
        }
    }
}

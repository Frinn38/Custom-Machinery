package fr.frinn.custommachinery.common.integration.kubejs;

import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.RecipeFunction;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.Scriptable;
import dev.latvian.mods.rhino.type.TypeInfo;

public class CustomRecipeFunction extends RecipeFunction {

    public CustomRecipeFunction(Context cx, Scriptable scope, TypeInfo staticType, KubeRecipe recipe) {
        super(cx, scope, staticType, recipe);
    }
}

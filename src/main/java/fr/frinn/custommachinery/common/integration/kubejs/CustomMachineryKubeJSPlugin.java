package fr.frinn.custommachinery.common.integration.kubejs;

import dev.latvian.kubejs.KubeJSPlugin;
import dev.latvian.kubejs.recipe.RegisterRecipeHandlersEvent;
import dev.latvian.kubejs.script.BindingsEvent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.kubejs.function.Result;

public class CustomMachineryKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void addRecipes(RegisterRecipeHandlersEvent event) {
        event.register(Registration.CUSTOM_MACHINE_RECIPE_SERIALIZER.getId(), CustomMachineJSRecipeBuilder::new);
    }

    @Override
    public void addBindings(BindingsEvent event) {
        event.addClass("Result", Result.class);
    }
}

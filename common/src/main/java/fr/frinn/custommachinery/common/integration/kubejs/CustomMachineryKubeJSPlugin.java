package fr.frinn.custommachinery.common.integration.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RegisterRecipeHandlersEvent;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.rhino.util.wrap.TypeWrappers;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.kubejs.function.MachineJS;
import fr.frinn.custommachinery.common.integration.kubejs.function.Result;
import fr.frinn.custommachinery.impl.util.IntRange;

public class CustomMachineryKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void addRecipes(RegisterRecipeHandlersEvent event) {
        event.register(Registration.CUSTOM_MACHINE_RECIPE_SERIALIZER.getId(), CustomMachineRecipeBuilderJS::new);
        event.register(Registration.CUSTOM_CRAFT_RECIPE_SERIALIZER.getId(), CustomCraftRecipeJSBuilder::new);
    }

    @Override
    public void addBindings(BindingsEvent event) {
        event.add("Result", Result.class);
        event.add("CustomMachine", MachineJS.class);
        event.add("CMRecipeModifierBuilder", CustomMachineUpgradeJSBuilder.JSRecipeModifierBuilder.class);
    }

    @Override
    public void addTypeWrappers(ScriptType type, TypeWrappers typeWrappers) {
        typeWrappers.register(IntRange.class, IntRange::of);
    }
}

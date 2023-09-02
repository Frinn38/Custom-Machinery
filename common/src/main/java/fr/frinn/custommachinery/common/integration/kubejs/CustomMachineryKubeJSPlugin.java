package fr.frinn.custommachinery.common.integration.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.rhino.util.wrap.TypeWrappers;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo.TooltipPredicate;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.kubejs.CustomMachineUpgradeJSBuilder.UpgradeEvent;
import fr.frinn.custommachinery.common.integration.kubejs.function.MachineJS;
import fr.frinn.custommachinery.common.integration.kubejs.function.Result;
import fr.frinn.custommachinery.impl.util.IntRange;

public class CustomMachineryKubeJSPlugin extends KubeJSPlugin {

    public static final EventGroup CM_EVENTS = EventGroup.of("CustomMachineryEvents");
    public static final EventHandler UPGRADES = CM_EVENTS.server("upgrades", () -> UpgradeEvent.class);

    @Override
    public void init() {
        RegistryInfo.BLOCK.addType(CustomMachinery.MODID, CustomMachineBlockBuilderJS.class, CustomMachineBlockBuilderJS::new);
    }

    @Override
    public void registerEvents() {
        CM_EVENTS.register();
    }

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        event.register(Registration.CUSTOM_MACHINE_RECIPE.getId(), CustomMachineryRecipeSchemas.CUSTOM_MACHINE);
        event.register(Registration.CUSTOM_CRAFT_RECIPE.getId(), CustomMachineryRecipeSchemas.CUSTOM_CRAFT);
    }

    @Override
    public void clearCaches() {
        AbstractRecipeJSBuilder.IDS.clear();
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("Result", Result.class);
        event.add("CustomMachine", MachineJS.class);
        event.add("CMRecipeModifierBuilder", CustomMachineUpgradeJSBuilder.JSRecipeModifierBuilder.class);
        event.add("TooltipPredicate", TooltipPredicate.class);
    }

    @Override
    public void registerTypeWrappers(ScriptType type, TypeWrappers typeWrappers) {
        typeWrappers.register(IntRange.class, (ctx, o) -> IntRange.of(o));
    }
}

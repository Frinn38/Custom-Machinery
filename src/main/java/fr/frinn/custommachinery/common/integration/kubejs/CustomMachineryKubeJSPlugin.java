package fr.frinn.custommachinery.common.integration.kubejs;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.EventTargetType;
import dev.latvian.mods.kubejs.event.TargetedEventHandler;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentTypeRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeFactoryRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import dev.latvian.mods.kubejs.recipe.schema.postprocessing.RecipePostProcessorTypeRegistry;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.script.TypeWrapperRegistry;
import dev.latvian.mods.rhino.type.TypeInfo;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo.TooltipPredicate;
import fr.frinn.custommachinery.common.integration.kubejs.CustomMachineUpgradeJSBuilder.UpgradeKubeEvent;
import fr.frinn.custommachinery.common.integration.kubejs.function.FunctionKubeEvent;
import fr.frinn.custommachinery.common.integration.kubejs.function.MachineJS;
import fr.frinn.custommachinery.common.util.BlockIngredient;
import fr.frinn.custommachinery.common.util.ComparatorMode;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import fr.frinn.custommachinery.impl.util.DoubleRange;
import fr.frinn.custommachinery.impl.util.IntRange;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;

public class CustomMachineryKubeJSPlugin implements KubeJSPlugin {

    public static final EventGroup CM_EVENTS = EventGroup.of("CustomMachineryEvents");
    public static final EventHandler UPGRADES = CM_EVENTS.server("upgrades", () -> UpgradeKubeEvent.class);
    public static final TargetedEventHandler<String> FUNCTIONS = CM_EVENTS.server("recipeFunction", () -> FunctionKubeEvent.class).hasResult(TypeInfo.of(Component.class)).requiredTarget(EventTargetType.STRING);

    @Override
    public void registerBuilderTypes(BuilderTypeRegistry registry) {
        registry.of(Registries.BLOCK, reg -> reg.add(CustomMachinery.rl("custom_machine"), CustomMachineBlockBuilderJS.class, CustomMachineBlockBuilderJS::new));
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(CM_EVENTS);
    }

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        //registry.register(Registration.CUSTOM_MACHINE_RECIPE.getId(), CustomMachineryRecipeSchemas.CUSTOM_MACHINE);
        //registry.register(Registration.CUSTOM_CRAFT_RECIPE.getId(), CustomMachineryRecipeSchemas.CUSTOM_CRAFT);
    }

    @Override
    public void registerRecipeComponents(RecipeComponentTypeRegistry registry) {
        registry.register(CustomMachineryRecipeComponents.RESOURCE_LOCATION);
        registry.register(CustomMachineryRecipeComponents.REQUIREMENT_COMPONENT);
        registry.register(CustomMachineryRecipeComponents.CUSTOM_APPEARANCE);
        registry.register(CustomMachineryRecipeComponents.CUSTOM_GUI_ELEMENTS);
    }

    @Override
    public void registerRecipeFactories(RecipeFactoryRegistry registry) {
        registry.register(CustomMachinery.rl("custom_machine"), CustomMachineRecipeBuilderJS.class, CustomMachineRecipeBuilderJS::new);
        registry.register(CustomMachinery.rl("custom_craft"), CustomCraftRecipeBuilderJS.class, CustomCraftRecipeBuilderJS::new);
    }

    @Override
    public void registerRecipePostProcessors(RecipePostProcessorTypeRegistry registry) {
        registry.register(RecipeIdPostProcessor.TYPE);
    }

    @Override
    public void beforeScriptsLoaded(ScriptManager manager) {
        RecipeIdPostProcessor.IDS.clear();
    }

    @Override
    public void registerBindings(BindingRegistry registry) {
        registry.add("CustomMachine", MachineJS.class);
        registry.add("CMRecipeModifierBuilder", CustomMachineUpgradeJSBuilder.JSRecipeModifierBuilder.class);
        registry.add("TooltipPredicate", TooltipPredicate.class);
    }

    @Override
    public void registerTypeWrappers(TypeWrapperRegistry registry) {
        registry.register(IntRange.class, IntRange::of);
        registry.register(DoubleRange.class, DoubleRange::of);
        registry.register(BlockIngredient.class, o -> {
            if(o instanceof CharSequence charSequence) {
                try {
                    return BlockIngredient.of(charSequence);
                } catch (CommandSyntaxException e) {
                    throw new IllegalArgumentException("Error parsing block ingredient: " + o + "\n" + e.getMessage());
                }
            }
            throw new IllegalArgumentException("Block ingredient must be a string");
        });
        registry.register(PartialBlockState.class, o -> {
            if(o instanceof CharSequence charSequence) {
                try {
                    return PartialBlockState.of(charSequence.toString());
                } catch (CommandSyntaxException e) {
                    throw new IllegalArgumentException("Error parsing BlockState: " + o + "\n" + e.getMessage());
                }
            }
            throw new IllegalArgumentException("BlockState must be a string");
        });
        registry.register(ComparatorMode.class, o -> {
            if(o instanceof CharSequence charSequence)
                return ComparatorMode.value(charSequence.toString());
            throw new IllegalArgumentException("Comparator must be a string");
        });
    }
}

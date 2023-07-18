package fr.frinn.custommachinery.common.integration.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ArrayRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.BooleanComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.component.TimeComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface CustomMachineryRecipeSchemas {

    RecipeComponent<String> RESOURCE_LOCATION = new StringComponent("machine", ResourceLocation::isValidResourceLocation);
    RecipeComponent<IRequirement<?>> REQUIREMENT_COMPONENT = new RecipeComponent<>() {
        @Override
        public Class<?> componentClass() {
            return IRequirement.class;
        }

        @Override
        public JsonElement write(RecipeJS recipe, IRequirement<?> value) {
            return IRequirement.CODEC.encodeStart(JsonOps.INSTANCE, value).result().orElseThrow(() -> new RecipeExceptionJS("Can't write requirement: " + value));
        }

        @Override
        public IRequirement<?> read(RecipeJS recipe, Object from) {
            if(from instanceof JsonElement json)
                return IRequirement.CODEC.read(JsonOps.INSTANCE, json).result().orElseThrow(() -> new RecipeExceptionJS("Can't parse requirement: " + from));
            throw new RecipeExceptionJS("Can't parse requirement: " + from);
        }
    };
    ArrayRecipeComponent<IRequirement<?>> REQUIREMENT_LIST = REQUIREMENT_COMPONENT.asArray();

    RecipeKey<String> MACHINE_ID = RESOURCE_LOCATION.key("machine");
    RecipeKey<Long> TIME = TimeComponent.TICKS.key("time");
    RecipeKey<OutputItem> OUTPUT = ItemComponents.OUTPUT.key("output");

    RecipeKey<IRequirement<?>[]> REQUIREMENTS = REQUIREMENT_LIST.key("requirements").optional(new IRequirement[0]).alwaysWrite().exclude();
    RecipeKey<IRequirement<?>[]> JEI_REQUIREMENTS = REQUIREMENT_LIST.key("jei").optional(new IRequirement[0]).alwaysWrite().exclude();

    RecipeKey<Integer> PRIORITY = NumberComponent.INT.key("priority").optional(0).alwaysWrite().exclude();
    RecipeKey<Integer> JEI_PRIORITY = NumberComponent.INT.key("jeiPriority").optional(0).alwaysWrite().exclude();

    RecipeKey<Boolean> ERROR = BooleanComponent.BOOLEAN.key("error").optional(false).alwaysWrite().exclude();
    RecipeKey<Boolean> HIDDEN = BooleanComponent.BOOLEAN.key("hidden").optional(false).alwaysWrite().exclude();

    RecipeSchema CUSTOM_MACHINE = new RecipeSchema(CustomMachineRecipeBuilderJS.class, CustomMachineRecipeBuilderJS::new, MACHINE_ID, TIME, REQUIREMENTS, JEI_REQUIREMENTS, PRIORITY, JEI_PRIORITY, ERROR, HIDDEN);
    RecipeSchema CUSTOM_CRAFT = new RecipeSchema(CustomCraftRecipeJSBuilder.class, CustomCraftRecipeJSBuilder::new, MACHINE_ID, OUTPUT, REQUIREMENTS, JEI_REQUIREMENTS, PRIORITY, JEI_PRIORITY, HIDDEN);
}

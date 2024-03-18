package fr.frinn.custommachinery.common.integration.kubejs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
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
import dev.latvian.mods.kubejs.recipe.component.TimeComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import net.minecraft.resources.ResourceLocation;

public interface CustomMachineryRecipeSchemas {

    RecipeComponent<ResourceLocation> RESOURCE_LOCATION = new RecipeComponent<ResourceLocation>() {
        @Override
        public Class<ResourceLocation> componentClass() {
            return ResourceLocation.class;
        }

        @Override
        public JsonElement write(RecipeJS recipe, ResourceLocation value) {
            return new JsonPrimitive(value.toString());
        }

        @Override
        public ResourceLocation read(RecipeJS recipe, Object from) {
            return new ResourceLocation(from instanceof JsonPrimitive json ? json.getAsString() : String.valueOf(from));
        }
    };
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
    RecipeComponent<MachineAppearance> CUSTOM_APPEARANCE = new RecipeComponent<>() {
        @Override
        public Class<MachineAppearance> componentClass() {
            return MachineAppearance.class;
        }

        @Override
        public JsonElement write(RecipeJS recipe, MachineAppearance value) {
            return MachineAppearance.CODEC.encodeStart(JsonOps.INSTANCE, value.getProperties()).result().orElse(null);
        }

        @Override
        public MachineAppearance read(RecipeJS recipe, Object from) {
            if(from instanceof MachineAppearance appearance)
                return appearance;

            if(from instanceof JsonObject json)
                return MachineAppearance.CODEC.read(JsonOps.INSTANCE, json).result().map(MachineAppearance::new).orElse(null);

            return null;
        }
    };

    RecipeComponent<IGuiElement> CUSTOM_GUI_ELEMENTS = new RecipeComponent<>() {
        @Override
        public Class<IGuiElement> componentClass() {
            return IGuiElement.class;
        }

        @Override
        public JsonElement write(RecipeJS recipe, IGuiElement value) {
            return IGuiElement.CODEC.encodeStart(JsonOps.INSTANCE, value).result().orElse(null);
        }

        @Override
        public IGuiElement read(RecipeJS recipe, Object from) {
            if(from instanceof IGuiElement element)
                return element;

            if(from instanceof JsonObject json)
                return IGuiElement.CODEC.read(JsonOps.INSTANCE, json).result().orElse(null);

            return null;
        }
    };

    RecipeKey<ResourceLocation> MACHINE_ID = RESOURCE_LOCATION.key("machine");
    RecipeKey<Long> TIME = TimeComponent.TICKS.key("time");
    RecipeKey<OutputItem> OUTPUT = ItemComponents.OUTPUT.key("output");

    RecipeKey<IRequirement<?>[]> REQUIREMENTS = REQUIREMENT_LIST.key("requirements").optional(new IRequirement[0]).alwaysWrite().exclude();
    RecipeKey<IRequirement<?>[]> JEI_REQUIREMENTS = REQUIREMENT_LIST.key("jei").optional(new IRequirement[0]).alwaysWrite().exclude();

    RecipeKey<Integer> PRIORITY = NumberComponent.INT.key("priority").optional(0).alwaysWrite().exclude();
    RecipeKey<Integer> JEI_PRIORITY = NumberComponent.INT.key("jeiPriority").optional(0).alwaysWrite().exclude();

    RecipeKey<Boolean> ERROR = BooleanComponent.BOOLEAN.key("error").optional(false).alwaysWrite().exclude();
    RecipeKey<Boolean> HIDDEN = BooleanComponent.BOOLEAN.key("hidden").optional(false).alwaysWrite().exclude();
    RecipeKey<MachineAppearance> APPEARANCE = CUSTOM_APPEARANCE.key("appearance").optional((MachineAppearance) null).alwaysWrite().exclude();
    RecipeKey<IGuiElement[]> GUI = CUSTOM_GUI_ELEMENTS.asArray().key("gui").optional(new IGuiElement[0]).alwaysWrite().exclude();

    RecipeSchema CUSTOM_MACHINE = new RecipeSchema(CustomMachineRecipeBuilderJS.class, CustomMachineRecipeBuilderJS::new, MACHINE_ID, TIME, REQUIREMENTS, JEI_REQUIREMENTS, PRIORITY, JEI_PRIORITY, ERROR, HIDDEN, APPEARANCE, GUI);
    RecipeSchema CUSTOM_CRAFT = new RecipeSchema(CustomCraftRecipeJSBuilder.class, CustomCraftRecipeJSBuilder::new, MACHINE_ID, OUTPUT, REQUIREMENTS, JEI_REQUIREMENTS, PRIORITY, JEI_PRIORITY, HIDDEN);
}

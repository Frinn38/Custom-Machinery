package fr.frinn.custommachinery.common.integration.kubejs;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import fr.frinn.custommachinery.common.crafting.craft.CustomCraftRecipeBuilder;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.*;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;

public class CustomCraftRecipeJSBuilder extends AbstractRecipeJSBuilder<CustomCraftRecipeBuilder> implements
        BiomeRequirementJS, BlockRequirementJS, CommandRequirementJS, DimensionRequirementJS, DropRequirementJS, DurabilityRequirementJS,
        EffectRequirementJS, EnergyRequirementJS, EntityRequirementJS, FluidRequirementJS, FuelRequirementJS, FunctionRequirementJS,
        ItemRequirementJS, ItemTransformRequirementJS, LightRequirementJS, LootTableRequirementJS, PositionRequirementJS, RedstoneRequirementJS,
        StructureRequirementJS, TimeRequirementJS, WeatherRequirementJS, ButtonRequirementJS, SkyRequirementJS, ItemFilterRequirementJS,
        ExperienceRequirementJS, ExperiencePerTickRequirementJS {

    public CustomCraftRecipeJSBuilder() {
        super(Registration.CUSTOM_CRAFT_RECIPE.getId());
    }

    @Override
    public CustomCraftRecipeBuilder makeBuilder() {
        return new CustomCraftRecipeBuilder(getValue(CustomMachineryRecipeSchemas.MACHINE_ID), getValue(CustomMachineryRecipeSchemas.OUTPUT).item);
    }

    @Override
    public JsonElement writeOutputItem(OutputItem value) {
        return DefaultCodecs.ITEM_OR_STACK.encodeStart(JsonOps.INSTANCE, value.item).result().orElseThrow(() -> new RecipeExceptionJS("Can't encode output item to nbt: " + value.item));
    }
}

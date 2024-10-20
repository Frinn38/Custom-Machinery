package fr.frinn.custommachinery.common.integration.kubejs;

import fr.frinn.custommachinery.common.crafting.craft.CustomCraftRecipeBuilder;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.BiomeRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.BlockRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.ButtonRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.CommandRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.DimensionRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.DropRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.DurabilityRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.EffectRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.EnergyRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.EntityRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.ExperienceRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.FluidRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.FuelRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.FunctionRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.ItemFilterRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.ItemRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.ItemTransformRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.LightRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.LootTableRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.PositionRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.RedstoneRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.SkyRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.StructureRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.TimeRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.WeatherRequirementJS;

public class CustomCraftRecipeBuilderJS extends AbstractRecipeJSBuilder<CustomCraftRecipeBuilder> implements
        BiomeRequirementJS, BlockRequirementJS, CommandRequirementJS, DimensionRequirementJS, DropRequirementJS, DurabilityRequirementJS,
        EffectRequirementJS, EnergyRequirementJS, EntityRequirementJS, FluidRequirementJS, FuelRequirementJS, FunctionRequirementJS,
        ItemRequirementJS, ItemTransformRequirementJS, LightRequirementJS, LootTableRequirementJS, PositionRequirementJS, RedstoneRequirementJS,
        StructureRequirementJS, TimeRequirementJS, WeatherRequirementJS, ButtonRequirementJS, SkyRequirementJS, ItemFilterRequirementJS,
        ExperienceRequirementJS {

    public CustomCraftRecipeBuilderJS() {
        super(Registration.CUSTOM_CRAFT_RECIPE.getId(), CustomCraftRecipeBuilder.CODEC);
    }

    @Override
    public CustomCraftRecipeBuilder makeBuilder() {
        return new CustomCraftRecipeBuilder(getValue(CustomMachineryRecipeSchemas.MACHINE_ID), getValue(CustomMachineryRecipeSchemas.OUTPUT).copy());
    }
}

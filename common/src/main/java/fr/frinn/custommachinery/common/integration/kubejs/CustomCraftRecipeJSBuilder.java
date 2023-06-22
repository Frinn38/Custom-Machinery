package fr.frinn.custommachinery.common.integration.kubejs;

import dev.latvian.mods.kubejs.recipe.RecipeArguments;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.common.crafting.craft.CustomCraftRecipeBuilder;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.BiomeRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.BlockRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.CommandRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.DimensionRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.DropRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.DurabilityRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.EffectRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.EnergyRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.EntityRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.FluidRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.FuelRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.FunctionRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.ItemRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.ItemTransformRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.LightRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.LootTableRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.PositionRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.RedstoneRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.StructureRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.TimeRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.WeatherRequirementJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.slf4j.helpers.MessageFormatter;

public class CustomCraftRecipeJSBuilder extends AbstractRecipeJSBuilder<CustomCraftRecipeBuilder> implements
        BiomeRequirementJS, BlockRequirementJS, CommandRequirementJS, DimensionRequirementJS, DropRequirementJS, DurabilityRequirementJS,
        EffectRequirementJS, EnergyRequirementJS, EntityRequirementJS, FluidRequirementJS, FuelRequirementJS, FunctionRequirementJS,
        ItemRequirementJS, ItemTransformRequirementJS, LightRequirementJS, LootTableRequirementJS, PositionRequirementJS, RedstoneRequirementJS,
        StructureRequirementJS, TimeRequirementJS, WeatherRequirementJS {

    public CustomCraftRecipeJSBuilder() {
        super(Registration.CUSTOM_CRAFT_RECIPE.getId(), CustomCraftRecipeBuilder.CODEC);
    }

    @Override
    public CustomCraftRecipeBuilder makeBuilder(ResourceLocation machine, RecipeArguments args) {
        if(args.size() < 2 || !(args.get(1) instanceof ItemStack output))
            throw new RecipeExceptionJS("Custom Craft recipe must have an output item specified");
        return new CustomCraftRecipeBuilder(machine, output);
    }

    @Override
    public CustomCraftRecipeJSBuilder addRequirement(IRequirement<?> requirement) {
        super.addRequirement(requirement);
        return this;
    }

    @Override
    public RecipeJSBuilder error(String error, Object... args) {
        throw new RecipeExceptionJS(MessageFormatter.arrayFormat(error, args).getMessage());
    }
}

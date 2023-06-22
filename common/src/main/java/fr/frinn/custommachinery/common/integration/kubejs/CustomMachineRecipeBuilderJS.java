package fr.frinn.custommachinery.common.integration.kubejs;

import dev.latvian.mods.kubejs.recipe.RecipeArguments;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.common.crafting.machine.CustomMachineRecipeBuilder;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.BiomeRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.BlockRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.CommandRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.DimensionRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.DropRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.DurabilityRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.EffectRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.EnergyPerTickRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.EnergyRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.EntityRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.FluidPerTickRequirementJS;
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
import org.slf4j.helpers.MessageFormatter;

public class CustomMachineRecipeBuilderJS extends AbstractRecipeJSBuilder<CustomMachineRecipeBuilder>
    implements ItemRequirementJS, ItemTransformRequirementJS, DurabilityRequirementJS, FluidRequirementJS, FluidPerTickRequirementJS,
        EnergyRequirementJS, EnergyPerTickRequirementJS, TimeRequirementJS, PositionRequirementJS, BiomeRequirementJS, DimensionRequirementJS,
        FuelRequirementJS, CommandRequirementJS, EffectRequirementJS, WeatherRequirementJS, RedstoneRequirementJS, LightRequirementJS,
        EntityRequirementJS, BlockRequirementJS, StructureRequirementJS, LootTableRequirementJS, DropRequirementJS, FunctionRequirementJS {

    public CustomMachineRecipeBuilderJS() {
        super(Registration.CUSTOM_MACHINE_RECIPE.getId(), CustomMachineRecipeBuilder.CODEC);
    }

    @Override
    public CustomMachineRecipeBuilder makeBuilder(ResourceLocation machine, RecipeArguments args) {
        if(args.size() < 2 || !(args.get(1) instanceof Number))
            throw new RecipeExceptionJS("Custom Machine recipe must have a machine and a time specified");
        return new CustomMachineRecipeBuilder(machine, args.getInt(1, 666));
    }

    @Override
    public CustomMachineRecipeBuilderJS addRequirement(IRequirement<?> requirement) {
        super.addRequirement(requirement);
        return this;
    }

    @Override
    public RecipeJSBuilder error(String error, Object... args) {
        throw new RecipeExceptionJS(MessageFormatter.arrayFormat(error, args).getMessage());
    }

    /** ERROR **/

    public CustomMachineRecipeBuilderJS resetOnError() {
        this.builder().setResetOnError();
        return this;
    }
}
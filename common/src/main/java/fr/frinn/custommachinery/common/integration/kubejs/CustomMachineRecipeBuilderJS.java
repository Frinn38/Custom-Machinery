package fr.frinn.custommachinery.common.integration.kubejs;

import fr.frinn.custommachinery.common.crafting.machine.CustomMachineRecipeBuilder;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.BiomeRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.BlockRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.ButtonRequirementJS;
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
import fr.frinn.custommachinery.common.integration.kubejs.requirements.SkyRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.StructureRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.TimeRequirementJS;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.WeatherRequirementJS;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class CustomMachineRecipeBuilderJS extends AbstractRecipeJSBuilder<CustomMachineRecipeBuilder>
    implements ItemRequirementJS, ItemTransformRequirementJS, DurabilityRequirementJS, FluidRequirementJS, FluidPerTickRequirementJS,
        EnergyRequirementJS, EnergyPerTickRequirementJS, TimeRequirementJS, PositionRequirementJS, BiomeRequirementJS, DimensionRequirementJS,
        FuelRequirementJS, CommandRequirementJS, EffectRequirementJS, WeatherRequirementJS, RedstoneRequirementJS, LightRequirementJS,
        EntityRequirementJS, BlockRequirementJS, StructureRequirementJS, LootTableRequirementJS, DropRequirementJS, FunctionRequirementJS,
        ButtonRequirementJS, SkyRequirementJS {

    @Nullable
    private MachineAppearance customAppearance = null;

    public CustomMachineRecipeBuilderJS() {
        super(Registration.CUSTOM_MACHINE_RECIPE.getId());
    }

    @Override
    public CustomMachineRecipeBuilder makeBuilder() {
        CustomMachineRecipeBuilder builder = new CustomMachineRecipeBuilder(getValue(CustomMachineryRecipeSchemas.MACHINE_ID), getValue(CustomMachineryRecipeSchemas.TIME).intValue());

        if(getValue(CustomMachineryRecipeSchemas.ERROR))
            builder.setResetOnError();

        if(this.customAppearance != null)
            builder.withAppearance(this.customAppearance);

        return builder;
    }

    /** ERROR **/

    public CustomMachineRecipeBuilderJS resetOnError() {
        setValue(CustomMachineryRecipeSchemas.ERROR, true);
        return this;
    }

    /* APPEARANCE */

    public CustomMachineRecipeBuilderJS appearance(Consumer<MachineAppearanceBuilderJS> consumer) {
        MachineAppearanceBuilderJS builder = new MachineAppearanceBuilderJS();
        consumer.accept(builder);
        this.customAppearance = builder.build();
        return this;
    }
}
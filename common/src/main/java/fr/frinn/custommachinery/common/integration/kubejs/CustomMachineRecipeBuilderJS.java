package fr.frinn.custommachinery.common.integration.kubejs;

import fr.frinn.custommachinery.common.crafting.machine.CustomMachineRecipeBuilder;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.kubejs.requirements.*;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class CustomMachineRecipeBuilderJS extends AbstractRecipeJSBuilder<CustomMachineRecipeBuilder>
    implements ItemRequirementJS, ItemTransformRequirementJS, DurabilityRequirementJS, FluidRequirementJS, FluidPerTickRequirementJS,
        EnergyRequirementJS, EnergyPerTickRequirementJS, TimeRequirementJS, PositionRequirementJS, BiomeRequirementJS, DimensionRequirementJS,
        FuelRequirementJS, CommandRequirementJS, EffectRequirementJS, WeatherRequirementJS, RedstoneRequirementJS, LightRequirementJS,
        EntityRequirementJS, BlockRequirementJS, StructureRequirementJS, LootTableRequirementJS, DropRequirementJS, FunctionRequirementJS,
        ButtonRequirementJS, SkyRequirementJS, ItemFilterRequirementJS, ExperienceRequirementJS, ExperiencePerTickRequirementJS {

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
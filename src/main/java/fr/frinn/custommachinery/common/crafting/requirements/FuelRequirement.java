package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.FuelMachineComponent;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.text.TranslationTextComponent;

public class FuelRequirement extends AbstractTickableRequirement<FuelMachineComponent> {

    public static final Codec<FuelRequirement> CODEC = RecordCodecBuilder.create(fuelRequirementInstance ->
            fuelRequirementInstance.group(
                    Codec.EMPTY.forGetter(requirement -> Unit.INSTANCE)
            ).apply(fuelRequirementInstance, unit -> new FuelRequirement())
    );

    public FuelRequirement() {
        super(MODE.INPUT);
    }

    @Override
    public RequirementType<?> getType() {
        return Registration.FUEL_REQUIREMENT.get();
    }

    @Override
    public boolean test(FuelMachineComponent component) {
        return true;
    }

    @Override
    public CraftingResult processStart(FuelMachineComponent component) {
        if(component.isBurning())
            return CraftingResult.success();
        return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.fuel.error"));
    }

    @Override
    public CraftingResult processTick(FuelMachineComponent component) {
        if(component.isBurning())
            return CraftingResult.success();
        return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.fuel.error"));
    }

    @Override
    public CraftingResult processEnd(FuelMachineComponent component) {
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<FuelMachineComponent> getComponentType() {
        return Registration.FUEL_MACHINE_COMPONENT.get();
    }
}

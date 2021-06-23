package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.FuelMachineComponent;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.IJEIRequirement;
import fr.frinn.custommachinery.common.integration.jei.RequirementDisplayInfo;
import net.minecraft.util.text.TranslationTextComponent;

public class FuelRequirement extends AbstractTickableRequirement<FuelMachineComponent> implements IJEIRequirement {

    public static final Codec<FuelRequirement> CODEC = Codec.unit(FuelRequirement::new);

    public FuelRequirement() {
        super(MODE.INPUT);
    }

    @Override
    public RequirementType<FuelRequirement> getType() {
        return Registration.FUEL_REQUIREMENT.get();
    }

    @Override
    public boolean test(FuelMachineComponent component, CraftingContext context) {
        return true;
    }

    @Override
    public CraftingResult processStart(FuelMachineComponent component, CraftingContext context) {
        if(component.isBurning())
            return CraftingResult.success();
        return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.fuel.error"));
    }

    @Override
    public CraftingResult processTick(FuelMachineComponent component, CraftingContext context) {
        if(component.isBurning())
            return CraftingResult.success();
        return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.fuel.error"));
    }

    @Override
    public CraftingResult processEnd(FuelMachineComponent component, CraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<FuelMachineComponent> getComponentType() {
        return Registration.FUEL_MACHINE_COMPONENT.get();
    }

    @Override
    public RequirementDisplayInfo getDisplayInfo() {
        return new RequirementDisplayInfo()
                .addTooltip(new TranslationTextComponent("custommachinery.requirements.fuel.info"));
    }
}

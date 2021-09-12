package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.components.MachineComponentType;
import fr.frinn.custommachinery.api.utils.CodecLogger;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.FuelMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.common.integration.jei.RequirementDisplayInfo;
import net.minecraft.util.text.TranslationTextComponent;

public class FuelRequirement extends AbstractTickableRequirement<FuelMachineComponent> implements IDisplayInfoRequirement<FuelMachineComponent> {

    public static final Codec<FuelRequirement> CODEC = RecordCodecBuilder.create(fuelRequirementInstance ->
            fuelRequirementInstance.group(
                    CodecLogger.loggedOptional(Codec.intRange(0, Integer.MAX_VALUE),"amount", 1).forGetter(requirement -> requirement.amount),
                    CodecLogger.loggedOptional(Codec.BOOL,"jei", true).forGetter(requirement -> requirement.jeiVisible)
            ).apply(fuelRequirementInstance, (amount, jei) -> {
                    FuelRequirement requirement = new FuelRequirement(amount);
                    requirement.setJeiVisible(jei);
                    return requirement;
            })
    );

    private int amount;
    private boolean jeiVisible = true;

    public FuelRequirement(int amount) {
        super(MODE.INPUT);
        this.amount = amount;
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
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processTick(FuelMachineComponent component, CraftingContext context) {
        if(component.burn(this.amount))
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
    public void setJeiVisible(boolean jeiVisible) {
        this.jeiVisible = jeiVisible;
    }

    @Override
    public RequirementDisplayInfo getDisplayInfo() {
        return new RequirementDisplayInfo()
                .setVisible(this.jeiVisible)
                .addTooltip(new TranslationTextComponent("custommachinery.requirements.fuel.info"));
    }
}

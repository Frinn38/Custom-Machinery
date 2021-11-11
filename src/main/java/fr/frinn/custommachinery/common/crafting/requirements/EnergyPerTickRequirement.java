package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.utils.CodecLogger;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.common.integration.jei.wrapper.EnergyIngredientWrapper;
import fr.frinn.custommachinery.common.util.Codecs;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Random;

public class EnergyPerTickRequirement extends AbstractTickableRequirement<EnergyMachineComponent> implements IChanceableRequirement<EnergyMachineComponent>, IJEIIngredientRequirement {

    public static final Codec<EnergyPerTickRequirement> CODEC = RecordCodecBuilder.create(energyPerTickRequirementInstance ->
            energyPerTickRequirementInstance.group(
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(AbstractTickableRequirement::getMode),
                    Codec.INT.fieldOf("amount").forGetter(requirement -> requirement.amount),
                    CodecLogger.loggedOptional(Codec.doubleRange(0.0, 1.0),"chance", 1.0D).forGetter(requirement -> requirement.chance)
            ).apply(energyPerTickRequirementInstance, (mode, amount, chance) -> {
                    EnergyPerTickRequirement requirement = new EnergyPerTickRequirement(mode, amount);
                    requirement.setChance(chance);
                    return requirement;
            })
    );

    private final int amount;
    private double chance = 1.0D;

    public EnergyPerTickRequirement(MODE mode, int amount) {
        super(mode);
        this.amount = amount;
    }

    @Override
    public RequirementType<EnergyPerTickRequirement> getType() {
        return Registration.ENERGY_PER_TICK_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType<EnergyMachineComponent> getComponentType() {
        return Registration.ENERGY_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean test(EnergyMachineComponent energy, CraftingContext context) {
        int amount = (int)context.getPerTickModifiedValue(this.amount, this, null);
        if(getMode() == MODE.INPUT)
            return energy.extractRecipeEnergy(amount, true) == amount;
        else
            return energy.receiveRecipeEnergy(amount, true) == amount;
    }

    @Override
    public CraftingResult processStart(EnergyMachineComponent energy, CraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processTick(EnergyMachineComponent energy, CraftingContext context) {
        int amount = (int)context.getPerTickModifiedValue(this.amount, this, null);
        if(getMode() == MODE.INPUT) {
            int canExtract = energy.extractRecipeEnergy(amount, true);
            if(canExtract == amount) {
                energy.extractRecipeEnergy(amount, false);
                return CraftingResult.success();
            }
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.energypertick.error.input", amount, canExtract));
        }
        else {
            int canReceive = energy.receiveRecipeEnergy(amount, true);
            if(canReceive == amount) {
                energy.receiveRecipeEnergy(amount, false);
                return CraftingResult.success();
            }
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.energypertick.error.output", amount));
        }
    }

    @Override
    public CraftingResult processEnd(EnergyMachineComponent energy, CraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public void setChance(double chance) {
        this.chance = MathHelper.clamp(chance, 0.0, 1.0);
    }

    @Override
    public boolean testChance(EnergyMachineComponent component, Random rand, CraftingContext context) {
        double chance = context.getModifiedvalue(this.chance, this, "chance");
        return rand.nextDouble() > chance;
    }

    @Override
    public EnergyIngredientWrapper getJEIIngredientWrapper() {
        return new EnergyIngredientWrapper(this.getMode(), this.amount, this.chance, true);
    }
}

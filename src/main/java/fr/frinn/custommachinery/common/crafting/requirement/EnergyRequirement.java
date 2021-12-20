package fr.frinn.custommachinery.common.crafting.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.codec.CodecLogger;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.api.requirement.IChanceableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.apiimpl.integration.jei.Energy;
import fr.frinn.custommachinery.apiimpl.requirement.AbstractRequirement;
import fr.frinn.custommachinery.common.data.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.wrapper.EnergyIngredientWrapper;
import fr.frinn.custommachinery.common.util.Codecs;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Lazy;

import java.util.Random;

public class EnergyRequirement extends AbstractRequirement<EnergyMachineComponent> implements IChanceableRequirement<EnergyMachineComponent>, IJEIIngredientRequirement<Energy> {

    public static final Codec<EnergyRequirement> CODEC = RecordCodecBuilder.create(energyRequirementInstance ->
            energyRequirementInstance.group(
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(AbstractRequirement::getMode),
                    Codec.INT.fieldOf("amount").forGetter(requirement -> requirement.amount),
                    CodecLogger.loggedOptional(Codec.doubleRange(0.0, 1.0),"chance", 1.0D).forGetter(requirement -> requirement.chance)
            ).apply(energyRequirementInstance, ((mode, amount, chance) -> {
                    EnergyRequirement requirement = new EnergyRequirement(mode, amount);
                    requirement.setChance(chance);
                    return requirement;
            }))
    );

    private final int amount;
    private double chance = 1.0D;
    private final Lazy<EnergyIngredientWrapper> wrapper;

    public EnergyRequirement(RequirementIOMode mode, int amount) {
        super(mode);
        this.amount = amount;
        this.wrapper = Lazy.of(() -> new EnergyIngredientWrapper(this.getMode(), this.amount, this.chance, false));
    }

    @Override
    public RequirementType<EnergyRequirement> getType() {
        return Registration.ENERGY_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType<EnergyMachineComponent> getComponentType() {
        return Registration.ENERGY_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean test(EnergyMachineComponent energy, ICraftingContext context) {
        int amount = (int)context.getModifiedValue(this.amount, this, null);
        if(getMode() == RequirementIOMode.INPUT)
            return energy.extractRecipeEnergy(amount, true) == amount;
        else
            return energy.receiveRecipeEnergy(amount, true) == amount;
    }

    @Override
    public CraftingResult processStart(EnergyMachineComponent energy, ICraftingContext context) {
        int amount = (int)context.getModifiedValue(this.amount, this, null);
        if(getMode() == RequirementIOMode.INPUT) {
            int canExtract = energy.extractRecipeEnergy(amount, true);
            if(canExtract == amount) {
                energy.extractRecipeEnergy(amount, false);
                return CraftingResult.success();
            }
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.energy.error.input", amount, canExtract));
        }
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(EnergyMachineComponent energy, ICraftingContext context) {
        int amount = (int)context.getModifiedValue(this.amount, this, null);
        if (getMode() == RequirementIOMode.OUTPUT) {
            int canReceive = energy.receiveRecipeEnergy(amount, true);
            if(canReceive == amount) {
                energy.receiveRecipeEnergy(amount, false);
                return CraftingResult.success();
            }
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.energy.error.output", amount));
        }
        return CraftingResult.pass();
    }

    @Override
    public void setChance(double chance) {
        this.chance = MathHelper.clamp(chance, 0.0, 1.0);
    }

    @Override
    public boolean shouldSkip(EnergyMachineComponent component, Random rand, ICraftingContext context) {
        double chance = context.getModifiedValue(this.chance, this, "chance");
        return rand.nextDouble() > chance;
    }

    @Override
    public EnergyIngredientWrapper getJEIIngredientWrapper() {
        return this.wrapper.get();
    }
}

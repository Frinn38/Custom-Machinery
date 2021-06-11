package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.common.integration.jei.wrapper.EnergyIngredientWrapper;
import fr.frinn.custommachinery.common.util.Codecs;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Lazy;

import java.util.Random;

public class EnergyRequirement extends AbstractRequirement<EnergyMachineComponent> implements IChanceableRequirement, IJEIIngredientRequirement {

    public static final Codec<EnergyRequirement> CODEC = RecordCodecBuilder.create(energyRequirementInstance ->
            energyRequirementInstance.group(
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(AbstractRequirement::getMode),
                    Codec.INT.fieldOf("amount").forGetter(requirement -> requirement.amount),
                    Codec.DOUBLE.optionalFieldOf("chance", 1.0D).forGetter(requirement -> requirement.chance)
            ).apply(energyRequirementInstance, EnergyRequirement::new)
    );

    private int amount;
    private double chance;

    public EnergyRequirement(MODE mode, int amount, double chance) {
        super(mode);
        this.amount = amount;
        this.chance = MathHelper.clamp(chance, 0.0D, 1.0D);;
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
    public boolean test(EnergyMachineComponent energy, CraftingContext context) {
        int amount = (int)context.getModifiedvalue(this.amount, this, null);
        if(getMode() == MODE.INPUT)
            return energy.extractRecipeEnergy(amount, true) == amount;
        else
            return energy.receiveRecipeEnergy(amount, true) == amount;
    }

    @Override
    public CraftingResult processStart(EnergyMachineComponent energy, CraftingContext context) {
        int amount = (int)context.getModifiedvalue(this.amount, this, null);
        if(getMode() == MODE.INPUT) {
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
    public CraftingResult processEnd(EnergyMachineComponent energy, CraftingContext context) {
        int amount = (int)context.getModifiedvalue(this.amount, this, null);
        if (getMode() == MODE.OUTPUT) {
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
    public boolean testChance(Random rand, CraftingContext context) {
        double chance = context.getModifiedvalue(this.chance, this, "chance");
        return rand.nextDouble() > chance;
    }

    private final Lazy<EnergyIngredientWrapper> jeiIngredientWrapper = Lazy.of(() -> new EnergyIngredientWrapper(this.getMode(), this.amount, this.chance, false));
    @Override
    public EnergyIngredientWrapper getJEIIngredientWrapper() {
        return this.jeiIngredientWrapper.get();
    }
}

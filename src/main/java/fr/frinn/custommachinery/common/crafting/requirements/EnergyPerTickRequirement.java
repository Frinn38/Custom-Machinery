package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.common.integration.jei.wrapper.EnergyIngredientWrapper;
import fr.frinn.custommachinery.common.util.Codecs;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Lazy;

public class EnergyPerTickRequirement extends AbstractTickableRequirement<EnergyMachineComponent> implements IJEIIngredientRequirement {

    public static final Codec<EnergyPerTickRequirement> CODEC = RecordCodecBuilder.create(energyPerTickRequirementInstance ->
            energyPerTickRequirementInstance.group(
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(AbstractTickableRequirement::getMode),
                    Codec.INT.fieldOf("amount").forGetter(requirement -> requirement.amount)
            ).apply(energyPerTickRequirementInstance, EnergyPerTickRequirement::new)
    );

    private int amount;

    public EnergyPerTickRequirement(MODE mode, int amount) {
        super(mode);
        this.amount = amount;
    }

    @Override
    public RequirementType getType() {
        return Registration.ENERGY_PER_TICK_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType<EnergyMachineComponent> getComponentType() {
        return Registration.ENERGY_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean test(EnergyMachineComponent energy) {
        return true;
    }

    @Override
    public CraftingResult processStart(EnergyMachineComponent energy) {
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processTick(EnergyMachineComponent energy) {
        if(getMode() == MODE.INPUT) {
            int canExtract = energy.extractRecipeEnergy(this.amount, true);
            if(canExtract == this.amount) {
                energy.extractRecipeEnergy(this.amount, false);
                return CraftingResult.success();
            }
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.energypertick.error.input", this.amount, canExtract));
        }
        else {
            int canReceive = energy.receiveRecipeEnergy(this.amount, true);
            if(canReceive == this.amount) {
                energy.receiveRecipeEnergy(this.amount, false);
                return CraftingResult.success();
            }
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.energypertick.error.output", this.amount));
        }
    }

    @Override
    public CraftingResult processEnd(EnergyMachineComponent energy) {
        return CraftingResult.pass();
    }

    private final Lazy<EnergyIngredientWrapper> jeiIngredientWrapper = Lazy.of(() -> new EnergyIngredientWrapper(this.getMode(), this.amount));
    @Override
    public EnergyIngredientWrapper getJEIIngredientWrapper() {
        return this.jeiIngredientWrapper.get();
    }
}

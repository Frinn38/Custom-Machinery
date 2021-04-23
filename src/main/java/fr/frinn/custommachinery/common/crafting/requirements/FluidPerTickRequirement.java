package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;
import fr.frinn.custommachinery.common.data.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.init.Registration;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;

public class FluidPerTickRequirement extends AbstractTickableRequirement<FluidComponentHandler> {

    @SuppressWarnings("deprecation")
    public static final Codec<FluidPerTickRequirement> CODEC = RecordCodecBuilder.create(fluidPerTickRequirementInstance ->
            fluidPerTickRequirementInstance.group(
                    Codec.STRING.fieldOf("mode").forGetter(requirement -> requirement.getMode().toString()),
                    Registry.FLUID.fieldOf("fluid").forGetter(requirement -> requirement.fluid),
                    Codec.INT.fieldOf("amount").forGetter(requirement -> requirement.amount)
            ).apply(fluidPerTickRequirementInstance, (mode, fluid, amount) -> new FluidPerTickRequirement(MODE.value(mode), fluid, amount))
    );

    private Fluid fluid;
    private int amount;

    public FluidPerTickRequirement(MODE mode, Fluid fluid, int amount) {
        super(mode);
        this.fluid = fluid;
        this.amount = amount;
    }

    @Override
    public RequirementType getType() {
        return Registration.FLUID_PER_TICK_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType getComponentType() {
        return Registration.FLUID_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean test(FluidComponentHandler component) {
        return true;
    }

    @Override
    public CraftingResult processStart(FluidComponentHandler component) {
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processTick(FluidComponentHandler component) {
        FluidStack stack = new FluidStack(this.fluid, this.amount);
        if(getMode() == MODE.INPUT) {
            int canExtract = component.getFluidAmount(this.fluid);
            if(canExtract >= this.amount) {
                component.removeFromInputs(stack);
                return CraftingResult.success();
            }
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.fluidpertick.error.input", this.fluid.getRegistryName(), this.amount, canExtract));
        }
        else {
            int canInsert = component.getSpaceForFluid(this.fluid);
            if(canInsert >= this.amount) {
                component.addToOutputs(stack);
                return CraftingResult.success();
            }
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.fluidpertick.error.output", this.amount, this.fluid.getRegistryName()));
        }
    }

    @Override
    public CraftingResult processEnd(FluidComponentHandler component) {
        return CraftingResult.pass();
    }

    @Override
    public IIngredientType<?> getJEIIngredientType() {
        return VanillaTypes.ITEM;
    }

    @Override
    public Object asJEIIngredient() {
        return new FluidStack(this.fluid, this.amount);
    }

    @Override
    public void addJeiIngredients(IIngredients ingredients) {
        if(getMode() == MODE.INPUT)
            ingredients.setInput(VanillaTypes.FLUID, new FluidStack(this.fluid, this.amount));
        else
            ingredients.setOutput(VanillaTypes.FLUID, new FluidStack(this.fluid, this.amount));
    }
}

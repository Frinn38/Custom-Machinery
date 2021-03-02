package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;
import fr.frinn.custommachinery.common.data.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
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
            return CraftingResult.error(new StringTextComponent("Not enough " + this.fluid.getRegistryName() + ", " + this.amount + "mB needed but " + canExtract + "mB found !"));
        }
        else {
            int canInsert = component.getSpaceForFluid(this.fluid);
            if(canInsert >= this.amount) {
                component.addToOutputs(stack);
                return CraftingResult.success();
            }
            return CraftingResult.error(new StringTextComponent("Not enough space for " + this.amount + "mB of " + this.fluid.getRegistryName()));
        }
    }

    @Override
    public CraftingResult processEnd(FluidComponentHandler component) {
        return CraftingResult.pass();
    }
}

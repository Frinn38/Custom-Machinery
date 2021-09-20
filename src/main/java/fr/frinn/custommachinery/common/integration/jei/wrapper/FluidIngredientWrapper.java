package fr.frinn.custommachinery.common.integration.jei.wrapper;

import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class FluidIngredientWrapper implements IJEIIngredientWrapper<FluidStack> {

    private IRequirement.MODE mode;
    private IIngredient<Fluid> fluid;
    private int amount;
    private double chance;
    private boolean isPerTick;
    private CompoundNBT nbt;
    private String tank;

    public FluidIngredientWrapper(IRequirement.MODE mode, IIngredient<Fluid> fluid, int amount, double chance, boolean isPerTick, CompoundNBT nbt, String tank) {
        this.mode = mode;
        this.fluid = fluid;
        this.amount = amount;
        this.chance = chance;
        this.isPerTick = isPerTick;
        this.nbt = nbt;
        this.tank = tank;
    }

    @Override
    public IIngredientType<FluidStack> getJEIIngredientType() {
        return VanillaTypes.FLUID;
    }

    @Override
    public Object asJEIIngredient() {
        return this.fluid.getAll().stream().map(fluid ->
            new FluidStackWrapper(fluid, this.amount, this.nbt).setPerTick(this.isPerTick).setSpecificTank(!this.tank.isEmpty()).setChance(this.chance)
        ).collect(Collectors.toList());
    }

    @Override
    public List<FluidStack> getJeiIngredients() {
        return this.fluid.getAll().stream().map(fluid -> new FluidStack(fluid, this.amount)).collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public String getComponentID() {
        return this.tank;
    }

    public static class FluidStackWrapper extends FluidStack {

        private boolean isPerTick;
        private boolean specificTank;
        private double chance;

        public FluidStackWrapper(Fluid fluid, int amount, CompoundNBT nbt) {
            super(fluid, amount, nbt);
        }

        public boolean isPerTick() {
            return this.isPerTick;
        }

        public FluidStackWrapper setPerTick(boolean perTick) {
            this.isPerTick = perTick;
            return this;
        }

        public boolean isSpecificTank() {
            return this.specificTank;
        }

        public FluidStackWrapper setSpecificTank(boolean specificTank) {
            this.specificTank = specificTank;
            return this;
        }

        public double getChance() {
            return this.chance;
        }

        public FluidStackWrapper setChance(double chance) {
            this.chance = chance;
            return this;
        }
    }
}

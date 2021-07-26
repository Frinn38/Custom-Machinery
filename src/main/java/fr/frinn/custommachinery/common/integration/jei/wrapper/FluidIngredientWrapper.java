package fr.frinn.custommachinery.common.integration.jei.wrapper;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.util.Ingredient;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class FluidIngredientWrapper implements IJEIIngredientWrapper<FluidStack> {

    private IRequirement.MODE mode;
    private Ingredient.FluidIngredient fluid;
    private int amount;
    private double chance;
    private boolean isPerTick;
    private String tank;

    public FluidIngredientWrapper(IRequirement.MODE mode, Ingredient.FluidIngredient fluid, int amount, double chance, boolean isPerTick, String tank) {
        this.mode = mode;
        this.fluid = fluid;
        this.amount = amount;
        this.chance = chance;
        this.isPerTick = isPerTick;
        this.tank = tank;
    }

    @Override
    public IIngredientType<FluidStack> getJEIIngredientType() {
        return VanillaTypes.FLUID;
    }

    @Override
    public Object asJEIIngredient() {
        List<FluidStack> stacks = this.fluid.getAll().stream().map(fluid -> new FluidStack(fluid, this.amount)).collect(Collectors.toList());
        if(this.isPerTick)
            stacks.forEach(stack -> stack.getOrCreateChildTag(CustomMachinery.MODID).putBoolean("isPerTick", true));
        if(this.chance != 1.0D)
            stacks.forEach(stack -> stack.getOrCreateChildTag(CustomMachinery.MODID).putDouble("chance", this.chance));
        if(!this.tank.isEmpty())
            stacks.forEach(stack -> stack.getOrCreateChildTag(CustomMachinery.MODID).putBoolean("specificTank", true));
        return stacks;
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
}

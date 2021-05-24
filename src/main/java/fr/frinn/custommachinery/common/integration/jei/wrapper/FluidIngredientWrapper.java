package fr.frinn.custommachinery.common.integration.jei.wrapper;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.tags.ITag;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FluidIngredientWrapper implements IJEIIngredientWrapper<FluidStack> {

    private IRequirement.MODE mode;
    private Fluid fluid;
    private int amount;
    private ITag<Fluid> tag;
    private double chance;
    private boolean isPerTick;

    public FluidIngredientWrapper(IRequirement.MODE mode, Fluid fluid, int amount, ITag<Fluid> tag, double chance, boolean isPerTick) {
        this.mode = mode;
        this.fluid = fluid;
        this.amount = amount;
        this.tag = tag;
        this.chance = chance;
        this.isPerTick = isPerTick;
    }

    @Override
    public IIngredientType<FluidStack> getJEIIngredientType() {
        return VanillaTypes.FLUID;
    }

    @Override
    public Object asJEIIngredient() {
        if(this.fluid != null && this.fluid != Fluids.EMPTY) {
            FluidStack stack = new FluidStack(this.fluid, this.amount);
            if(this.isPerTick)
                stack.getOrCreateChildTag(CustomMachinery.MODID).putBoolean("isPerTick", true);
            if(this.chance != 1.0D)
                stack.getOrCreateChildTag(CustomMachinery.MODID).putDouble("chance", this.chance);
            return stack;
        }
        else if(this.tag != null && this.mode == IRequirement.MODE.INPUT) {
            List<FluidStack> stacks = this.tag.getAllElements().stream().map(fluid -> new FluidStack(fluid, this.amount)).collect(Collectors.toList());
            if(this.isPerTick)
                stacks.forEach(stack -> stack.getOrCreateChildTag(CustomMachinery.MODID).putBoolean("isPerTick", true));
            if(this.chance != 1.0D)
                stacks.forEach(stack -> stack.getOrCreateChildTag(CustomMachinery.MODID).putDouble("chance", this.chance));
            return stacks;
        }
        else throw new IllegalStateException("Using Fluid Requirement with null item and/or tag");
    }

    @Override
    public List<FluidStack> getJeiIngredients() {
        if(this.fluid != null && this.fluid != Fluids.EMPTY)
            return Collections.singletonList(new FluidStack(this.fluid, this.amount));
        else if(this.tag != null && this.mode == IRequirement.MODE.INPUT)
            return this.tag.getAllElements().stream().map(fluid -> new FluidStack(fluid, this.amount)).collect(Collectors.toList());
        else throw new IllegalStateException("Using Fluid Requirement with null item and/or tag");
    }
}

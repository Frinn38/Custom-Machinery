package fr.frinn.custommachinery.common.integration.jei.wrapper;

import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.integration.jei.IRecipeHelper;
import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.data.gui.FluidGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FluidIngredientWrapper implements IJEIIngredientWrapper<FluidStack> {

    private final IRequirement.MODE mode;
    private final IIngredient<Fluid> fluid;
    private final int amount;
    private final double chance;
    private final boolean isPerTick;
    private final CompoundNBT nbt;
    private final String tank;

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
    public void setIngredient(IIngredients ingredients) {
        List<FluidStack> fluids = this.fluid.getAll().stream().map(fluid -> new FluidStack(fluid, this.amount, this.nbt)).collect(Collectors.toList());
        if(this.mode == IRequirement.MODE.INPUT)
            ingredients.setInputs(VanillaTypes.FLUID, fluids);
        else
            ingredients.setOutputs(VanillaTypes.FLUID, fluids);
    }

    @Override
    public boolean setupRecipe(int index, IRecipeLayout layout, int xOffset, int yOffset, IGuiElement element, IIngredientRenderer<FluidStack> renderer, IRecipeHelper helper) {
        if(!(element instanceof FluidGuiElement) || element.getType() != Registration.FLUID_GUI_ELEMENT.get())
            return false;

        List<FluidStack> ingredients = this.fluid.getAll().stream().map(fluid -> new FluidStack(fluid, this.amount, this.nbt)).collect(Collectors.toList());
        FluidGuiElement fluidElement = (FluidGuiElement)element;
        Optional<IMachineComponentTemplate<?>> template = helper.getComponentForElement(fluidElement);
        if(template.map(t -> t.canAccept(ingredients, this.mode == IRequirement.MODE.INPUT, helper.getDummyManager())).orElse(false)) {
            layout.getIngredientsGroup(getJEIIngredientType()).init(index, this.mode == IRequirement.MODE.INPUT, renderer, element.getX() - xOffset, element.getY() - yOffset, element.getWidth() - 2, element.getHeight() - 2, 0, 0);
            IGuiIngredientGroup<FluidStack> group = layout.getIngredientsGroup(VanillaTypes.FLUID);
            group.set(index, ingredients);
            group.addTooltipCallback(((slotIndex, input, ingredient, tooltips) -> {
                if(slotIndex != index)
                    return;
                if(this.isPerTick)
                    tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.fluid.pertick", this.amount));
                else
                    tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.fluid", this.amount));

                if(this.chance == 0)
                    tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.chance.0").mergeStyle(TextFormatting.DARK_RED));
                else if(this.chance != 1.0)
                    tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.chance", (int)(this.chance * 100)));

                if(!this.tank.isEmpty() && Minecraft.getInstance().gameSettings.advancedItemTooltips)
                    tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.fluid.specificTank").mergeStyle(TextFormatting.DARK_RED));
            }));
            return true;
        }
        return false;
    }
}

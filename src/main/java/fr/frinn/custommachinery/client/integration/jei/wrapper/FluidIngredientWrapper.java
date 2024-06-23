package fr.frinn.custommachinery.client.integration.jei.wrapper;

import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.integration.jei.IRecipeHelper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.guielement.FluidGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.neoforge.NeoForgeTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.Optional;

public class FluidIngredientWrapper implements IJEIIngredientWrapper<FluidStack> {

    private final RequirementIOMode mode;
    private final IIngredient<Fluid> fluid;
    private final long amount;
    private final double chance;
    private final boolean isPerTick;
    private final String tank;

    public FluidIngredientWrapper(RequirementIOMode mode, IIngredient<Fluid> fluid, long amount, double chance, boolean isPerTick, String tank) {
        this.mode = mode;
        this.fluid = fluid;
        this.amount = amount;
        this.chance = chance;
        this.isPerTick = isPerTick;
        this.tank = tank;
    }

    @Override
    public boolean setupRecipe(IRecipeLayoutBuilder builder, int xOffset, int yOffset, IGuiElement element, IRecipeHelper helper) {
        if(!(element instanceof FluidGuiElement fluidElement) || element.getType() != Registration.FLUID_GUI_ELEMENT.get())
            return false;

        List<FluidStack> ingredients = this.fluid.getAll().stream().map(fluid -> new FluidStack(fluid, (int)this.amount)).toList();
        Optional<IMachineComponentTemplate<?>> template = helper.getComponentForElement(fluidElement);
        if(fluidElement.getComponentId().equals(this.tank) || template.map(t -> t.canAccept(ingredients, this.mode == RequirementIOMode.INPUT, helper.getDummyManager()) && (this.tank.isEmpty() || t.getId().equals(this.tank))).orElse(false)) {
            IRecipeSlotBuilder slot = builder.addSlot(roleFromMode(this.mode), element.getX() - xOffset + 1, element.getY() - yOffset + 1)
                    .setFluidRenderer(this.amount, false, element.getWidth() - 2, element.getHeight() - 2)
                    .addTooltipCallback((view, tooltips) -> {
                        if(this.isPerTick)
                            tooltips.add(Component.translatable("custommachinery.jei.ingredient.fluid.pertick"));

                        if(this.chance == 0)
                            tooltips.add(Component.translatable("custommachinery.jei.ingredient.chance.0").withStyle(ChatFormatting.DARK_RED));
                        else if(this.chance != 1.0)
                            tooltips.add(Component.translatable("custommachinery.jei.ingredient.chance", (int)(this.chance * 100)));

                        if(!this.tank.isEmpty() && Minecraft.getInstance().options.advancedItemTooltips)
                            tooltips.add(Component.translatable("custommachinery.jei.ingredient.fluid.specificTank").withStyle(ChatFormatting.DARK_RED));
                    });
            slot.addIngredients(NeoForgeTypes.FLUID_STACK, this.fluid.getAll().stream().map(fluid -> new FluidStack(fluid, (int)this.amount)).toList());
            return true;
        }
        return false;
    }
}

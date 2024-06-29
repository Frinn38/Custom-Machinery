package fr.frinn.custommachinery.client.integration.jei.wrapper;

import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.integration.jei.IRecipeHelper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.guielement.FluidGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.neoforge.NeoForgeTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class FluidIngredientWrapper implements IJEIIngredientWrapper<FluidStack> {

    private final RequirementIOMode mode;
    private final SizedFluidIngredient ingredient;
    private final double chance;
    private final boolean isPerTick;
    private final String tank;

    public FluidIngredientWrapper(RequirementIOMode mode, SizedFluidIngredient ingredient, double chance, boolean isPerTick, String tank) {
        this.mode = mode;
        this.ingredient = ingredient;
        this.chance = chance;
        this.isPerTick = isPerTick;
        this.tank = tank;
    }

    @Override
    public boolean setupRecipe(IRecipeLayoutBuilder builder, int xOffset, int yOffset, IGuiElement element, IRecipeHelper helper) {
        if(!(element instanceof FluidGuiElement fluidElement) || element.getType() != Registration.FLUID_GUI_ELEMENT.get())
            return false;

        List<FluidStack> ingredients = Arrays.stream(this.ingredient.getFluids()).map(fluid -> fluid.copyWithAmount(this.ingredient.amount())).toList();
        Optional<IMachineComponentTemplate<?>> template = helper.getComponentForElement(fluidElement);
        if(fluidElement.getComponentId().equals(this.tank) || template.map(t -> t.canAccept(ingredients, this.mode == RequirementIOMode.INPUT, helper.getDummyManager()) && (this.tank.isEmpty() || t.getId().equals(this.tank))).orElse(false)) {
            builder.addSlot(roleFromMode(this.mode), element.getX() - xOffset + 1, element.getY() - yOffset + 1)
                    .setFluidRenderer(this.ingredient.amount(), false, element.getWidth() - 2, element.getHeight() - 2)
                    .addIngredients(NeoForgeTypes.FLUID_STACK, ingredients)
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
            return true;
        }
        return false;
    }
}

package fr.frinn.custommachinery.forge.client;

import dev.architectury.hooks.fluid.forge.FluidStackHooksForge;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.integration.jei.IRecipeHelper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.guielement.FluidGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.Optional;

public class LegacyJei9FluidIngredientWrapper implements IJEIIngredientWrapper<FluidStack> {
    private final RequirementIOMode mode;
    private final IIngredient<Fluid> fluid;
    private final int amount;
    private final double chance;
    private final boolean isPerTick;
    private final CompoundTag nbt;
    private final String tank;

    public LegacyJei9FluidIngredientWrapper(RequirementIOMode mode, IIngredient<Fluid> fluid, int amount, double chance, boolean isPerTick, CompoundTag nbt, String tank) {
        this.mode = mode;
        this.fluid = fluid;
        this.amount = amount;
        this.chance = chance;
        this.isPerTick = isPerTick;
        this.nbt = nbt;
        this.tank = tank;
    }

    @Override
    public boolean setupRecipe(IRecipeLayoutBuilder builder, int xOffset, int yOffset, IGuiElement element, IRecipeHelper helper) {
        if(!(element instanceof FluidGuiElement fluidElement) || element.getType() != Registration.FLUID_GUI_ELEMENT.get())
            return false;

        List<FluidStack> ingredients = this.fluid.getAll().stream().map(fluid -> new FluidStack(fluid, this.amount, this.nbt)).toList();
        Optional<IMachineComponentTemplate<?>> template = helper.getComponentForElement(fluidElement);
        if(fluidElement.getID().equals(this.tank) || template.map(t -> t.canAccept(ingredients.stream().map(FluidStackHooksForge::fromForge).toList(), this.mode == RequirementIOMode.INPUT, helper.getDummyManager()) && (this.tank.isEmpty() || t.getId().equals(this.tank))).orElse(false)) {
            builder.addSlot(roleFromMode(this.mode), element.getX() - xOffset, element.getY() - yOffset)
                    .setFluidRenderer(this.amount, false, element.getWidth() - 2, element.getHeight() - 2)
                    .addIngredients(ForgeTypes.FLUID_STACK, ingredients)
                    .addTooltipCallback((view, tooltips) -> {
                        if(this.isPerTick)
                            tooltips.add(new TranslatableComponent("custommachinery.jei.ingredient.fluid.pertick"));

                        if(this.chance == 0)
                            tooltips.add(new TranslatableComponent("custommachinery.jei.ingredient.chance.0").withStyle(ChatFormatting.DARK_RED));
                        else if(this.chance != 1.0)
                            tooltips.add(new TranslatableComponent("custommachinery.jei.ingredient.chance", (int)(this.chance * 100)));

                        if(!this.tank.isEmpty() && Minecraft.getInstance().options.advancedItemTooltips)
                            tooltips.add(new TranslatableComponent("custommachinery.jei.ingredient.fluid.specificTank").withStyle(ChatFormatting.DARK_RED));
                    });
            return true;
        }
        return false;
    }
}

package fr.frinn.custommachinery.client.integration.jei.energy;

import fr.frinn.custommachinery.api.integration.jei.JEIIngredientRenderer;
import fr.frinn.custommachinery.common.guielement.EnergyGuiElement;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.integration.jei.CustomIngredientTypes;
import fr.frinn.custommachinery.impl.integration.jei.Energy;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EnergyJEIIngredientRenderer extends JEIIngredientRenderer<Energy, EnergyGuiElement> {

    public EnergyJEIIngredientRenderer(EnergyGuiElement element) {
        super(element);
    }

    @Override
    public IIngredientType<Energy> getType() {
        return CustomIngredientTypes.ENERGY;
    }

    @Override
    public int getWidth() {
        return this.element.getWidth() - 2;
    }

    @Override
    public int getHeight() {
        return this.element.getHeight() - 2;
    }

    @Override
    public void render(GuiGraphics graphics, @Nullable Energy ingredient) {
        int width = this.element.getWidth();
        int height = this.element.getHeight();

        graphics.blit(this.element.getFilledTexture(), -1, -1,0, 0, width, height, width, height);
    }

    @Override
    public List<Component> getTooltip(Energy ingredient, TooltipFlag iTooltipFlag) {
        List<Component> tooltips = new ArrayList<>();
        String amount = Utils.format(ingredient.getAmount());
        Component unit = Component.translatable("unit.energy.forge");
        if(ingredient.isPerTick())
            tooltips.add(Component.translatable("custommachinery.jei.ingredient.energy.pertick", amount, unit));
        else
            tooltips.add(Component.translatable("custommachinery.jei.ingredient.energy", amount, unit));
        if(ingredient.getChance() == 0)
            tooltips.add(Component.translatable("custommachinery.jei.ingredient.chance.0").withStyle(ChatFormatting.DARK_RED));
        if(ingredient.getChance() < 1.0D && ingredient.getChance() > 0)
            tooltips.add(Component.translatable("custommachinery.jei.ingredient.chance", (int)(ingredient.getChance() * 100)));
        return tooltips;
    }
}

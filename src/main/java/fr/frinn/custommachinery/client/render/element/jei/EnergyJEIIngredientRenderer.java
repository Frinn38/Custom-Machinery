package fr.frinn.custommachinery.client.render.element.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.integration.jei.JEIIngredientRenderer;
import fr.frinn.custommachinery.apiimpl.integration.jei.CustomIngredientTypes;
import fr.frinn.custommachinery.apiimpl.integration.jei.Energy;
import fr.frinn.custommachinery.common.data.gui.EnergyGuiElement;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nullable;
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
    public void render(PoseStack matrix, EnergyGuiElement element, @Nullable Energy ingredient) {
        int width = element.getWidth();
        int height = element.getHeight();
        if(ingredient != null)
            Minecraft.getInstance().getTextureManager().bindForSetup(element.getFilledTexture());
        else
            Minecraft.getInstance().getTextureManager().bindForSetup(element.getEmptyTexture());
        GuiComponent.blit(matrix, -1, -1, 0, 0, width, height, width, height);
    }

    @Override
    public List<Component> getTooltip(Energy ingredient, EnergyGuiElement element, TooltipFlag iTooltipFlag) {
        List<Component> tooltips = new ArrayList<>();
        if(ingredient.isPerTick())
            tooltips.add(new TranslatableComponent("custommachinery.jei.ingredient.energy.pertick", ingredient.getAmount()));
        else
            tooltips.add(new TranslatableComponent("custommachinery.jei.ingredient.energy", ingredient.getAmount()));
        if(ingredient.getChance() == 0)
            tooltips.add(new TranslatableComponent("custommachinery.jei.ingredient.chance.0").withStyle(ChatFormatting.DARK_RED));
        if(ingredient.getChance() < 1.0D && ingredient.getChance() > 0)
            tooltips.add(new TranslatableComponent("custommachinery.jei.ingredient.chance", (int)(ingredient.getChance() * 100)));
        return tooltips;
    }
}

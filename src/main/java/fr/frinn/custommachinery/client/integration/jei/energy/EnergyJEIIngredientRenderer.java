package fr.frinn.custommachinery.client.integration.jei.energy;

import fr.frinn.custommachinery.api.integration.jei.JEIIngredientRenderer;
import fr.frinn.custommachinery.common.guielement.EnergyGuiElement;
import fr.frinn.custommachinery.impl.integration.jei.CustomIngredientTypes;
import fr.frinn.custommachinery.impl.integration.jei.Energy;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

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

        graphics.pose().pushPose();
        //Translate to make sure the filled texture is rendered on top of empty texture.
        graphics.pose().translate(0, 0, 10);
        graphics.blit(this.element.getFilledTexture(), -1, -1,0, 0, width, height, width, height);
        graphics.pose().popPose();
    }

    //Safe to remove
    @SuppressWarnings("removal")
    @Override
    public List<Component> getTooltip(Energy ingredient, TooltipFlag tooltipFlag) {
        return List.of();
    }

    @Override
    public void getTooltip(ITooltipBuilder builder, Energy ingredient, TooltipFlag tooltipFlag) {

    }
}

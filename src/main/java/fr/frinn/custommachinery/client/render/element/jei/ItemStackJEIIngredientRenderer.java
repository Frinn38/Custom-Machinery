package fr.frinn.custommachinery.client.render.element.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.frinn.custommachinery.common.data.gui.SlotGuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class ItemStackJEIIngredientRenderer extends JEIIngredientRenderer<ItemStack, SlotGuiElement> {

    public ItemStackJEIIngredientRenderer(SlotGuiElement element) {
        super(element);
    }

    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    @Override
    public void render(MatrixStack matrix, int x, int y, SlotGuiElement element, @Nullable ItemStack ingredient) {
        int width = element.getWidth();
        int height = element.getHeight();
        Minecraft.getInstance().getTextureManager().bindTexture(element.getTexture());
        AbstractGui.blit(matrix, x - 1, y - 1, 0, 0, width, height, width, height);
        if(ingredient != null) {
            RenderSystem.pushMatrix();
            RenderSystem.scalef(0.5F, 0.5F, 0.5F);
            RenderSystem.translatef(x, y, 0);
            Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(ingredient, x, y);
            Minecraft.getInstance().getItemRenderer().renderItemOverlayIntoGUI(getFontRenderer(Minecraft.getInstance(), ingredient), ingredient, x, y, null);
            RenderSystem.popMatrix();
        }
    }

    @ParametersAreNonnullByDefault
    @Override
    public List<ITextComponent> getTooltip(ItemStack ingredient, SlotGuiElement element, ITooltipFlag tooltipFlag) {
        return ingredient.getTooltip(null, tooltipFlag);
    }
}

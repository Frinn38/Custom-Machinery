package fr.frinn.custommachinery.client.render.element.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.api.guielement.jei.JEIIngredientRenderer;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.data.gui.SlotGuiElement;
import mezz.jei.api.MethodsReturnNonnullByDefault;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemStackJEIIngredientRenderer extends JEIIngredientRenderer<ItemStack, SlotGuiElement> {

    public ItemStackJEIIngredientRenderer(SlotGuiElement element) {
        super(element);
    }

    @Override
    public IIngredientType<ItemStack> getType() {
        return VanillaTypes.ITEM;
    }

    @Override
    public void render(MatrixStack matrix, int x, int y, SlotGuiElement element, @Nullable ItemStack ingredient) {
        int width = element.getWidth();
        int height = element.getHeight();
        Minecraft.getInstance().getTextureManager().bindTexture(element.getTexture());
        AbstractGui.blit(matrix, x - 1, y - 1, 0, 0, width, height, width, height);
        if(ingredient != null) {
            ClientHandler.renderItemAndEffectsIntoGUI(matrix, ingredient, x, y);
            ClientHandler.renderItemOverlayIntoGUI(matrix, getFontRenderer(Minecraft.getInstance(), ingredient), ingredient, x, y, null);
        }
    }

    @Override
    public List<ITextComponent> getTooltip(ItemStack ingredient, SlotGuiElement element, ITooltipFlag flag) {
        return ingredient.getTooltip(null, flag);
    }

    @Override
    public FontRenderer getFontRenderer(Minecraft minecraft, ItemStack ingredient) {
        return Optional.ofNullable(ingredient.getItem().getFontRenderer(ingredient)).orElse(super.getFontRenderer(minecraft, ingredient));
    }
}

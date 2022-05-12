package fr.frinn.custommachinery.client.render.element.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.integration.jei.JEIIngredientRenderer;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.data.gui.SlotGuiElement;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ItemStackJEIIngredientRenderer extends JEIIngredientRenderer<ItemStack, SlotGuiElement> {

    public ItemStackJEIIngredientRenderer(SlotGuiElement element) {
        super(element);
    }

    @Override
    public IIngredientType<ItemStack> getType() {
        return VanillaTypes.ITEM_STACK;
    }

    @Override
    public void render(@NotNull PoseStack matrix, SlotGuiElement element, @Nullable ItemStack ingredient) {
        int width = element.getWidth();
        int height = element.getHeight();
        ClientHandler.bindTexture(element.getTexture());
        GuiComponent.blit(matrix, -1, -1, 0, 0, width, height, width, height);
        if(ingredient != null) {
            ClientHandler.renderItemAndEffectsIntoGUI(matrix, ingredient, 0, 0);
            ClientHandler.renderItemOverlayIntoGUI(matrix, getFontRenderer(Minecraft.getInstance(), ingredient), ingredient, 0, 0, null);
        }
    }

    @Override
    public List<Component> getTooltip(@NotNull ItemStack ingredient, SlotGuiElement element, TooltipFlag flag) {
        return ingredient.getTooltipLines(null, flag);
    }

    @Override
    public Font getFontRenderer(@NotNull Minecraft minecraft, @NotNull ItemStack ingredient) {
        return Minecraft.getInstance().font;
    }
}

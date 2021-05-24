package fr.frinn.custommachinery.client.render.element.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.data.gui.SlotGuiElement;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class ItemStackJEIIngredientRenderer extends JEIIngredientRenderer<ItemStack, SlotGuiElement> {

    public ItemStackJEIIngredientRenderer(SlotGuiElement element) {
        super(element);
    }

    @Override
    public IIngredientType<ItemStack> getType() {
        return VanillaTypes.ITEM;
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
            if(ingredient.getTag() != null)
                ingredient.getTag().remove(CustomMachinery.MODID);
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
        List<ITextComponent> tooltips = ingredient.getTooltip(null, tooltipFlag);
        if(ingredient.getChildTag(CustomMachinery.MODID) != null) {
            if(ingredient.getChildTag(CustomMachinery.MODID).contains("consumeDurability")) {
                int durability = ingredient.getChildTag(CustomMachinery.MODID).getInt("consumeDurability");
                tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.item.durability.consume", durability));
            } else if(ingredient.getChildTag(CustomMachinery.MODID).contains("repairDurability")) {
                int durability = ingredient.getChildTag(CustomMachinery.MODID).getInt("repairDurability");
                tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.item.durability.repair", durability));
            }
            if(ingredient.getChildTag(CustomMachinery.MODID).contains("chance")) {
                double chance = ingredient.getChildTag(CustomMachinery.MODID).getDouble("chance");
                tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.chance", (int) (chance * 100)));
            }
        }
        return tooltips;
    }
}

package fr.frinn.custommachinery.client.render.element.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.guielement.jei.JEIIngredientRenderer;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.data.gui.SlotGuiElement;
import mezz.jei.api.MethodsReturnNonnullByDefault;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

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
        List<ITextComponent> tooltips = ingredient.getTooltip(null, flag);
        CompoundNBT nbt = ingredient.getChildTag(CustomMachinery.MODID);
        if(nbt == null)
            return tooltips;
        if(nbt.contains("consumeDurability", Constants.NBT.TAG_INT)) {
            int durability = nbt.getInt("consumeDurability");
            tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.item.durability.consume", durability));
        } else if(nbt.contains("repairDurability", Constants.NBT.TAG_INT)) {
            int durability = nbt.getInt("repairDurability");
            tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.item.durability.repair", durability));
        }
        if(nbt.contains("chance", Constants.NBT.TAG_DOUBLE)) {
            double chance = nbt.getDouble("chance");
            if(chance == 0)
                tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.chance.0").mergeStyle(TextFormatting.DARK_RED));
            else
                tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.chance", (int) (chance * 100)));
        }
        if(nbt.contains("specificSlot", Constants.NBT.TAG_BYTE) && nbt.getBoolean("specificSlot") && flag.isAdvanced())
            tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.item.specificSlot").mergeStyle(TextFormatting.DARK_RED));
        return tooltips;
    }
}

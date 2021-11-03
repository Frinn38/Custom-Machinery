package fr.frinn.custommachinery.client.render.element.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.guielement.jei.JEIIngredientRenderer;
import fr.frinn.custommachinery.common.data.gui.EnergyGuiElement;
import fr.frinn.custommachinery.common.integration.jei.CustomIngredientTypes;
import fr.frinn.custommachinery.common.integration.jei.energy.Energy;
import mezz.jei.api.MethodsReturnNonnullByDefault;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EnergyJEIIngredientRenderer extends JEIIngredientRenderer<Energy, EnergyGuiElement> {

    private static final ResourceLocation EMPTY = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_empty.png");

    public EnergyJEIIngredientRenderer(EnergyGuiElement element) {
        super(element);
    }

    @Override
    public IIngredientType<Energy> getType() {
        return CustomIngredientTypes.ENERGY;
    }

    @Override
    public void render(MatrixStack matrix, int x, int y, EnergyGuiElement element, @Nullable Energy ingredient) {
        int width = element.getWidth();
        int height = element.getHeight();
        Minecraft.getInstance().getTextureManager().bindTexture(element.getEmptyTexture());
        AbstractGui.blit(matrix, x - 1, y - 1, 0, 0, width, height, width, height);
        if(ingredient != null) {
            double fillPercent = 1.0D;
            int eneryHeight = (int)(fillPercent * (double)(height - 2));
            Minecraft.getInstance().getTextureManager().bindTexture(element.getFilledTexture());
            AbstractGui.blit(matrix, x, y + height - eneryHeight, 0, height - eneryHeight, width, eneryHeight, width, height);
        }
    }

    @Override
    public List<ITextComponent> getTooltip(Energy ingredient, EnergyGuiElement element, ITooltipFlag iTooltipFlag) {
        List<ITextComponent> tooltips = new ArrayList<>();
        if(ingredient.isPerTick())
            tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.energy.pertick", ingredient.getAmount()));
        else
            tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.energy", ingredient.getAmount()));
        if(ingredient.getChance() == 0)
            tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.chance.0").mergeStyle(TextFormatting.DARK_RED));
        if(ingredient.getChance() < 1.0D && ingredient.getChance() > 0)
            tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.chance", (int)(ingredient.getChance() * 100)));
        return tooltips;
    }
}

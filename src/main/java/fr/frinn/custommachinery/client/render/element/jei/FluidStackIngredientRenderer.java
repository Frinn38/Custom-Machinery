package fr.frinn.custommachinery.client.render.element.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.frinn.custommachinery.common.data.gui.FluidGuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

public class FluidStackIngredientRenderer extends JEIIngredientRenderer<FluidStack, FluidGuiElement> {

    public FluidStackIngredientRenderer(FluidGuiElement element) {
        super(element);
    }

    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    @Override
    public void render(MatrixStack matrix, int x, int y, FluidGuiElement element, @Nullable FluidStack fluid) {
        int width = element.getWidth();
        int height = element.getHeight();
        Minecraft.getInstance().getTextureManager().bindTexture(element.getTexture());
        AbstractGui.blit(matrix, x - 1, y - 1, 0, 0, width, height, width, height);
        if(fluid != null) {
            ResourceLocation fluidTexture = fluid.getFluid().getAttributes().getStillTexture();
            TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(fluidTexture);
            int color = fluid.getFluid().getAttributes().getColor();
            float filledPercent = (float)fluid.getAmount() / (float)fluid.getAmount();
            int fluidHeight = (int)(height * filledPercent);
            RenderSystem.color4f(((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, ((color >> 0) & 0xFF) / 255f, ((color >> 24) & 0xFF) / 255f);
            Minecraft.getInstance().getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
            AbstractGui.blit(matrix, x - 1 + 1, y - 1 + (height - fluidHeight + 1), 0, width - 2, fluidHeight - 2, sprite);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @ParametersAreNonnullByDefault
    @Override
    public List<ITextComponent> getTooltip(FluidStack ingredient, FluidGuiElement element, ITooltipFlag tooltipFlag) {
        List<ITextComponent> tooltips = new ArrayList<>();
        tooltips.add(ingredient.getDisplayName());
        tooltips.add(new TranslationTextComponent("custommachinery.jei.fluid.amount", ingredient.getAmount()));
        return tooltips;
    }
}

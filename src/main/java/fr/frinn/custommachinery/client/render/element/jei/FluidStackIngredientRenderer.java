package fr.frinn.custommachinery.client.render.element.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.api.guielement.jei.JEIIngredientRenderer;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.client.TextureSizeHelper;
import fr.frinn.custommachinery.common.data.gui.FluidGuiElement;
import fr.frinn.custommachinery.common.integration.jei.wrapper.FluidIngredientWrapper;
import fr.frinn.custommachinery.common.util.Color3F;
import mezz.jei.api.MethodsReturnNonnullByDefault;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
public class FluidStackIngredientRenderer extends JEIIngredientRenderer<FluidStack, FluidGuiElement> {

    public FluidStackIngredientRenderer(FluidGuiElement element) {
        super(element);
    }

    @Override
    public IIngredientType<FluidStack> getType() {
        return VanillaTypes.FLUID;
    }

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
            int textureWidth = TextureSizeHelper.getTextureWidth(element.getTexture());
            float xScale = (float) width / (float) textureWidth;
            matrix.push();
            matrix.translate(x - 1, y - 1, 0);
            matrix.scale(xScale, 1.0F, 1.0F);
            matrix.translate(-x + 1, -y + 1, 0);
            ClientHandler.renderFluidInTank(matrix, x, y + height - 2, fluidHeight - 2, sprite, Color3F.of(color));
            matrix.pop();
        }
    }

    @ParametersAreNonnullByDefault
    @Override
    public List<ITextComponent> getTooltip(FluidStack ingredient, FluidGuiElement element, ITooltipFlag flag) {
        List<ITextComponent> tooltips = new ArrayList<>();
        tooltips.add(ingredient.getDisplayName());
        if(ingredient instanceof FluidIngredientWrapper.FluidStackWrapper) {
            FluidIngredientWrapper.FluidStackWrapper wrapper = (FluidIngredientWrapper.FluidStackWrapper)ingredient;
            if(wrapper.isPerTick())
                tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.fluid.pertick", wrapper.getAmount()));
            else
                tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.fluid", wrapper.getAmount()));

            if(wrapper.getChance() == 0)
                tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.chance.0").mergeStyle(TextFormatting.DARK_RED));
            else if(wrapper.getChance() != 1.0)
                tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.chance", (int)(wrapper.getChance() * 100)));

            if(wrapper.isSpecificTank() && flag.isAdvanced())
                tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.fluid.specificTank").mergeStyle(TextFormatting.DARK_RED));
        }
        return tooltips;
    }
}

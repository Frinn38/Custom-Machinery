package fr.frinn.custommachinery.client.render.element.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.data.gui.FluidGuiElement;
import fr.frinn.custommachinery.common.util.Color3F;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
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
            ClientHandler.renderFluidInTank(matrix, x, y, height - fluidHeight, fluidHeight - 2, sprite, Color3F.of(color));
        }
    }

    @ParametersAreNonnullByDefault
    @Override
    public List<ITextComponent> getTooltip(FluidStack ingredient, FluidGuiElement element, ITooltipFlag tooltipFlag) {
        List<ITextComponent> tooltips = new ArrayList<>();
        tooltips.add(ingredient.getDisplayName());
        if(ingredient.getChildTag(CustomMachinery.MODID) != null && ingredient.getChildTag(CustomMachinery.MODID).contains("isPerTick") && ingredient.getChildTag(CustomMachinery.MODID).getBoolean("isPerTick"))
            tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.fluid.pertick", ingredient.getAmount()));
        else
            tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.fluid", ingredient.getAmount()));
        if(ingredient.getChildTag(CustomMachinery.MODID) != null && ingredient.getChildTag(CustomMachinery.MODID).contains("chance")) {
            double chance = ingredient.getChildTag(CustomMachinery.MODID).getDouble("chance");
            tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.chance", (int)(chance * 100)));
        }
        return tooltips;
    }
}

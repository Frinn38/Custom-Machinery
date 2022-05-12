package fr.frinn.custommachinery.client.render.element.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.integration.jei.JEIIngredientRenderer;
import fr.frinn.custommachinery.api.utils.TextureSizeHelper;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.data.gui.FluidGuiElement;
import fr.frinn.custommachinery.common.util.Color3F;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class FluidStackIngredientRenderer extends JEIIngredientRenderer<FluidStack, FluidGuiElement> {

    public FluidStackIngredientRenderer(FluidGuiElement element) {
        super(element);
    }

    @Override
    public IIngredientType<FluidStack> getType() {
        return ForgeTypes.FLUID_STACK;
    }

    @Override
    public void render(PoseStack matrix, FluidGuiElement element, @Nullable FluidStack fluid) {
        int width = element.getWidth();
        int height = element.getHeight();
        ClientHandler.bindTexture(element.getTexture());
        GuiComponent.blit(matrix, -1, -1, 0, 0, width, height, width, height);
        if(fluid != null) {
            ResourceLocation fluidTexture = fluid.getFluid().getAttributes().getStillTexture();
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidTexture);
            int color = fluid.getFluid().getAttributes().getColor();
            float filledPercent = (float)fluid.getAmount() / (float)fluid.getAmount();
            int fluidHeight = (int)(height * filledPercent);
            int textureWidth = TextureSizeHelper.getTextureWidth(element.getTexture());
            float xScale = (float) width / (float) textureWidth;
            matrix.pushPose();
            matrix.translate(-1, -1, 0);
            matrix.scale(xScale, 1.0F, 1.0F);
            matrix.translate(1, 1, 0);
            ClientHandler.renderFluidInTank(matrix, 0, height - 2, fluidHeight - 2, sprite, Color3F.of(color));
            matrix.popPose();
        }
    }

    @Override
    public List<Component> getTooltip(FluidStack ingredient, FluidGuiElement element, TooltipFlag flag) {
        List<Component> tooltips = new ArrayList<>();
        tooltips.add(ingredient.getDisplayName());
        return tooltips;
    }
}

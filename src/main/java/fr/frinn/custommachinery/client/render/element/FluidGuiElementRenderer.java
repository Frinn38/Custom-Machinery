package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.frinn.custommachinery.client.TextureSizeHelper;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.common.data.gui.FluidGuiElement;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;

public class FluidGuiElementRenderer implements IGuiElementRenderer<FluidGuiElement> {

    @SuppressWarnings("deprecation")
    @Override
    public void renderElement(MatrixStack matrix, FluidGuiElement element, CustomMachineScreen screen) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth() < 0 ? TextureSizeHelper.getTextureWidth(element.getTexture()) : element.getWidth();
        int height = element.getHeight() < 0 ? TextureSizeHelper.getTextureHeight(element.getTexture()) : element.getHeight();
        screen.getMinecraft().getTextureManager().bindTexture(element.getTexture());
        AbstractGui.blit(matrix, posX, posY, 0, 0, width, height, width, height);
        screen.getTile().componentManager.getFluidHandler().ifPresent(fluidHandler -> {
            fluidHandler.getComponentForId(element.getId()).ifPresent(component -> {
                FluidStack fluid = component.getFluidStack();
                ResourceLocation fluidTexture = fluid.getFluid().getAttributes().getStillTexture();
                TextureAtlasSprite sprite = screen.getMinecraft().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(fluidTexture);
                int color = fluid.getFluid().getAttributes().getColor();
                float filledPercent = (float)fluid.getAmount() / (float)component.getCapacity();
                int fluidHeight = (int)(height * filledPercent);
                RenderSystem.color4f(((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, ((color >> 0) & 0xFF) / 255f, ((color >> 24) & 0xFF) / 255f);
                screen.getMinecraft().getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
                AbstractGui.blit(matrix, posX + 1, posY + (height - fluidHeight + 1), 0, width - 2, fluidHeight - 2, sprite);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            });
        });
    }

    @Override
    public void renderTooltip(MatrixStack matrix, FluidGuiElement element, CustomMachineScreen screen, int mouseX, int mouseY) {
        screen.getTile().componentManager.getFluidHandler().ifPresent(fluidHandler -> {
            fluidHandler.getComponentForId(element.getId()).ifPresent(component -> {
                String fluid = component.getFluidStack().getTranslationKey();
                int amount = component.getFluidStack().getAmount();
                int capacity = component.getCapacity();
                screen.renderTooltip(matrix, new TranslationTextComponent(fluid).append(new TranslationTextComponent("custommachinery.gui.element.fluid.tooltip", amount, capacity)), mouseX, mouseY);
            });
        });
    }

    @Override
    public boolean isHovered(FluidGuiElement element, CustomMachineScreen screen, int mouseX, int mouseY) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth() > 0 ? element.getWidth() : TextureSizeHelper.getTextureWidth(element.getTexture());
        int height = element.getHeight() > 0 ? element.getHeight() : TextureSizeHelper.getTextureHeight(element.getTexture());
        return mouseX >= posX && mouseX <= posX + width && mouseY >= posY && mouseY <= posY + height;
    }
}

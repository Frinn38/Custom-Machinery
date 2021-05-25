package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.common.data.gui.FluidGuiElement;
import fr.frinn.custommachinery.common.util.Color3F;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;

public class FluidGuiElementRenderer implements IGuiElementRenderer<FluidGuiElement> {

    @Override
    public void renderElement(MatrixStack matrix, FluidGuiElement element, CustomMachineScreen screen) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();
        screen.getMinecraft().getTextureManager().bindTexture(element.getTexture());
        AbstractGui.blit(matrix, posX, posY, 0, 0, width, height, width, height);
        screen.getTile().componentManager.getFluidHandler().flatMap(fluidHandler -> fluidHandler.getComponentForID(element.getID())).ifPresent(component -> {
            FluidStack fluid = component.getFluidStack();
            ResourceLocation fluidTexture = fluid.getFluid().getAttributes().getStillTexture();
            TextureAtlasSprite sprite = screen.getMinecraft().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(fluidTexture);
            int color = fluid.getFluid().getAttributes().getColor();
            float filledPercent = (float) fluid.getAmount() / (float) component.getCapacity();
            int fluidHeight = (int) (height * filledPercent);
            ClientHandler.renderFluidInTank(matrix, posX + 1, posY + 1, height - fluidHeight, fluidHeight - 2, sprite, Color3F.of(color));
        });
    }

    @Override
    public void renderTooltip(MatrixStack matrix, FluidGuiElement element, CustomMachineScreen screen, int mouseX, int mouseY) {
        screen.getTile().componentManager.getFluidHandler().flatMap(fluidHandler -> fluidHandler.getComponentForID(element.getID())).ifPresent(component -> {
            String fluid = component.getFluidStack().getTranslationKey();
            int amount = component.getFluidStack().getAmount();
            int capacity = component.getCapacity();
            screen.renderTooltip(matrix, new TranslationTextComponent(fluid).appendSibling(new TranslationTextComponent("custommachinery.gui.element.fluid.tooltip", amount, capacity)), mouseX, mouseY);
        });
    }

    @Override
    public boolean isHovered(FluidGuiElement element, CustomMachineScreen screen, int mouseX, int mouseY) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();
        return mouseX >= posX && mouseX <= posX + width && mouseY >= posY && mouseY <= posY + height;
    }
}

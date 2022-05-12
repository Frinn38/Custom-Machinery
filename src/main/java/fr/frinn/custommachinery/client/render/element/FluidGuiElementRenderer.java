package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.guielement.IGuiElementRenderer;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.api.utils.TextureSizeHelper;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.data.gui.FluidGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Color3F;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.fluids.FluidStack;

public class FluidGuiElementRenderer implements IGuiElementRenderer<FluidGuiElement> {

    @Override
    public void renderElement(PoseStack matrix, FluidGuiElement element, IMachineScreen screen) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();
        ClientHandler.bindTexture(element.getTexture());
        GuiComponent.blit(matrix, posX, posY, 0, 0, width, height, width, height);
        screen.getTile().getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).flatMap(fluidHandler -> fluidHandler.getComponentForID(element.getID())).ifPresent(component -> {
            FluidStack fluid = component.getFluidStack();
            ResourceLocation fluidTexture = fluid.getFluid().getAttributes().getStillTexture();
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidTexture);
            int color = fluid.getFluid().getAttributes().getColor();
            float filledPercent = (float) fluid.getAmount() / (float) component.getCapacity();
            int fluidHeight = (int) (height * filledPercent);
            int textureWidth = TextureSizeHelper.getTextureWidth(element.getTexture());
            float xScale = (float) width / (float) textureWidth;
            matrix.pushPose();
            matrix.translate(posX, posY, 0);
            matrix.scale(xScale, 1.0F, 1.0F);
            matrix.translate(-posX, -posY, 0);
            ClientHandler.renderFluidInTank(matrix, posX + 1, posY + height - 1, fluidHeight - 2, sprite, Color3F.of(color));
            matrix.popPose();
        });
    }

    @Override
    public void renderTooltip(PoseStack matrix, FluidGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {
        screen.getTile().getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).flatMap(fluidHandler -> fluidHandler.getComponentForID(element.getID())).ifPresent(component -> {
            String fluid = component.getFluidStack().getTranslationKey();
            int amount = component.getFluidStack().getAmount();
            int capacity = component.getCapacity();
            screen.getScreen().renderTooltip(matrix, new TranslatableComponent(fluid).append(new TranslatableComponent("custommachinery.gui.element.fluid.tooltip", amount, capacity)), mouseX, mouseY);
        });
    }
}

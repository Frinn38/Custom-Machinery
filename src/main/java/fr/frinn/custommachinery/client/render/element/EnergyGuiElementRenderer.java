package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.frinn.custommachinery.client.TextureSizeHelper;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.common.data.gui.EnergyGuiElement;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.text.TranslationTextComponent;

public class EnergyGuiElementRenderer implements IGuiElementRenderer<EnergyGuiElement> {

    @SuppressWarnings("deprecation")
    @Override
    public void renderElement(MatrixStack matrix, EnergyGuiElement element, CustomMachineScreen screen) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();
        screen.getMinecraft().getTextureManager().bindTexture(element.getTexture());
        AbstractGui.blit(matrix, posX, posY, 0, 0, width, height, width, height);
        screen.getTile().componentManager.getEnergy().ifPresent(energy -> {
            double fillPercent = (double)energy.getEnergyStored() / (double)energy.getMaxEnergyStored();
            int eneryHeight = (int)(fillPercent * (double)(height - 2));

            drawTransparentRec(matrix, posX + 1, posY + height - eneryHeight - 1, width - 2, eneryHeight);
        });
    }

    @SuppressWarnings("deprecation")
    private void drawTransparentRec(MatrixStack matrix, int x, int y, int width, int height) {
        RenderSystem.enableBlend();

        BufferBuilder builder = Tessellator.getInstance().getBuffer();
        builder.begin(7, DefaultVertexFormats.POSITION_COLOR);

        builder.pos(matrix.getLast().getMatrix(), x, y + height, 0).color(255, 0, 0, 200).endVertex();
        builder.pos(matrix.getLast().getMatrix(), x + width, y + height, 0).color(255, 0, 0, 200).endVertex();
        builder.pos(matrix.getLast().getMatrix(), x + width, y, 0).color(255, 0, 0, 200).endVertex();
        builder.pos(matrix.getLast().getMatrix(), x, y, 0).color(255, 0, 0, 200).endVertex();


        builder.finishDrawing();
        WorldVertexBufferUploader.draw(builder);

        RenderSystem.disableBlend();
    }

    @Override
    public void renderTooltip(MatrixStack matrix, EnergyGuiElement element, CustomMachineScreen screen, int mouseX, int mouseY) {
        screen.getTile().componentManager.getEnergy().ifPresent(energyComponent -> {
            int energy = energyComponent.getEnergyStored();
            int maxEnergy = energyComponent.getMaxEnergyStored();
            screen.renderTooltip(matrix, new TranslationTextComponent("custommachinery.gui.element.energy.tooltip", energy, maxEnergy), mouseX, mouseY);
        });

    }

    @Override
    public boolean isHovered(EnergyGuiElement element, CustomMachineScreen screen, int mouseX, int mouseY) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();
        return mouseX >= posX && mouseX <= posX + width && mouseY >= posY && mouseY <= posY + height;
    }
}

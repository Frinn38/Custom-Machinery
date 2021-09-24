package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.api.guielement.IGuiElementRenderer;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.common.data.gui.StatusGuiElement;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatusGuiElementRenderer implements IGuiElementRenderer<StatusGuiElement> {

    @Override
    public void renderElement(MatrixStack matrix, StatusGuiElement element, IMachineScreen screen) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();

        switch (screen.getTile().getStatus()) {
            case IDLE:
            case PAUSED:
                Minecraft.getInstance().getTextureManager().bindTexture(element.getIdleTexture());
                break;
            case RUNNING:
                Minecraft.getInstance().getTextureManager().bindTexture(element.getRunningTexture());
                break;
            case ERRORED:
                Minecraft.getInstance().getTextureManager().bindTexture(element.getErroredTexture());
                break;
        }
        AbstractGui.blit(matrix, posX, posY, 0, 0, width, height, width, height);
    }

    @Override
    public void renderTooltip(MatrixStack matrix, StatusGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {
        List<ITextComponent> tooltips = new ArrayList<>();
        tooltips.add(new TranslationTextComponent("custommachinery.craftingstatus." + screen.getTile().getStatus().toString().toLowerCase(Locale.ENGLISH)));
        if(screen.getTile().getStatus() == MachineStatus.ERRORED)
            tooltips.add(((CustomMachineTile)screen.getTile()).craftingManager.getErrorMessage());
        screen.getScreen().func_243308_b(matrix, tooltips, mouseX, mouseY);
    }

    @Override
    public boolean isHovered(StatusGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();

        return mouseX >= posX && mouseX <= posX + width && mouseY >= posY && mouseY <= posY + height;
    }
}

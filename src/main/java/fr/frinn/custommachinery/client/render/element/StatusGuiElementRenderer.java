package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.guielement.IGuiElementRenderer;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.data.gui.StatusGuiElement;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatusGuiElementRenderer implements IGuiElementRenderer<StatusGuiElement> {

    @Override
    public void renderElement(PoseStack matrix, StatusGuiElement element, IMachineScreen screen) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();

        switch (screen.getTile().getStatus()) {
            case IDLE, PAUSED -> ClientHandler.bindTexture(element.getIdleTexture());
            case RUNNING -> ClientHandler.bindTexture(element.getRunningTexture());
            case ERRORED -> ClientHandler.bindTexture(element.getErroredTexture());
        }
        GuiComponent.blit(matrix, posX, posY, 0, 0, width, height, width, height);
    }

    @Override
    public void renderTooltip(PoseStack matrix, StatusGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {
        List<Component> tooltips = new ArrayList<>();
        tooltips.add(new TranslatableComponent("custommachinery.craftingstatus." + screen.getTile().getStatus().toString().toLowerCase(Locale.ENGLISH)));
        if(screen.getTile().getStatus() == MachineStatus.ERRORED)
            tooltips.add(((CustomMachineTile)screen.getTile()).craftingManager.getErrorMessage());
        screen.getScreen().renderComponentTooltip(matrix, tooltips, mouseX, mouseY);
    }
}

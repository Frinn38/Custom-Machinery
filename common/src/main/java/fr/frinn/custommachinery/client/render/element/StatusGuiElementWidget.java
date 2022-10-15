package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.guielement.StatusGuiElement;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatusGuiElementWidget extends AbstractGuiElementWidget<StatusGuiElement> {

    public StatusGuiElementWidget(StatusGuiElement element, IMachineScreen screen) {
        super(element, screen, new TextComponent("Status"));
    }

    @Override
    public void renderButton(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        switch (this.getScreen().getTile().getStatus()) {
            case IDLE, PAUSED -> ClientHandler.bindTexture(this.getElement().getIdleTexture());
            case RUNNING -> ClientHandler.bindTexture(this.getElement().getRunningTexture());
            case ERRORED -> ClientHandler.bindTexture(this.getElement().getErroredTexture());
        }
        GuiComponent.blit(matrix, this.x, this.y, 0, 0, this.width, this.height, this.width, this.height);
    }

    @Override
    public List<Component> getTooltips() {
        List<Component> tooltips = new ArrayList<>();
        tooltips.add(new TranslatableComponent("custommachinery.craftingstatus." + this.getScreen().getTile().getStatus().toString().toLowerCase(Locale.ENGLISH)));
        if(this.getScreen().getTile().getStatus() == MachineStatus.ERRORED)
            tooltips.add(((CustomMachineTile)this.getScreen().getTile()).craftingManager.getErrorMessage());
        return tooltips;
    }
}

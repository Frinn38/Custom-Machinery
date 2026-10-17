package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.client.IMachineScreen;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.client.screen.ScreenUtils;
import fr.frinn.custommachinery.common.crafting.machine.MachineProcessor;
import fr.frinn.custommachinery.common.guielement.StatusGuiElement;
import fr.frinn.custommachinery.impl.client.AbstractGuiElementRenderable;
import fr.frinn.custommachinery.impl.util.TextureInfo;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class StatusGuiElementWidget extends AbstractGuiElementRenderable<StatusGuiElement> {

    public StatusGuiElementWidget(StatusGuiElement element, IMachineScreen screen) {
        super(element, screen);
    }

    @Override
    public void extractRenderableRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        MachineStatus status = MachineStatus.IDLE;
        if(this.getElement().getCore() == 0)
            status = this.getScreen().getTile().getStatus();
        else if(this.getScreen().getTile().getProcessor() instanceof MachineProcessor processor && processor.getCores().size() >= this.getElement().getCore())
            status = processor.getCores().get(this.getElement().getCore() - 1).getStatus();
        TextureInfo texture = switch(status) {
            case RUNNING -> this.getElement().getRunningTexture();
            case ERRORED -> this.getElement().getErroredTexture();
            default -> this.getElement().getIdleTexture();
        };
        ScreenUtils.blit(graphics, texture, this.getElement().getX(), this.getElement().getY(), this.getElement().getWidth(), this.getElement().getHeight());
    }

    @Override
    public List<Component> getTooltips() {
        if(!this.getElement().getTooltips().isEmpty())
            return this.getElement().getTooltips();
        List<Component> tooltips = new ArrayList<>();
        if(this.getScreen().getTile().getProcessor() instanceof MachineProcessor processor && processor.getCores().size() > 1) {
            processor.getCores().forEach(core -> {
                int index = processor.getCores().indexOf(core);
                if(core.getStatus() == MachineStatus.RUNNING)
                    tooltips.add(Component.empty().append(Component.literal("✓ ").withStyle(core.getStatus().getFormatting())).append(Component.literal("Core " + index + ": ")).append(core.getStatus().getTranslatedName()));
                else if(core.getStatus() == MachineStatus.IDLE)
                    tooltips.add(Component.empty().append(Component.literal("|| ").withStyle(core.getStatus().getFormatting())).append(Component.literal("Core " + index + ": ")).append(core.getStatus().getTranslatedName()));
                if(core.getStatus() == MachineStatus.ERRORED) {
                    tooltips.add(Component.empty().append(Component.literal("X ").withStyle(core.getStatus().getFormatting())).append(Component.literal("Core " + index + ": ")).append(core.getStatus().getTranslatedName()));
                    if(core.getError() != null)
                        tooltips.add(Component.literal("  - ").append(core.getError()));
                }
            });
        } else {
            MachineStatus status = this.getScreen().getTile().getStatus();
            tooltips.add(status.getTranslatedName());
            if(status == MachineStatus.ERRORED)
                tooltips.add(this.getScreen().getTile().getMessage());
        }
        return tooltips;
    }
}

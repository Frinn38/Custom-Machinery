package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.crafting.machine.MachineProcessor;
import fr.frinn.custommachinery.common.guielement.StatusGuiElement;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;
import fr.frinn.custommachinery.impl.util.TextureInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatusGuiElementWidget extends AbstractGuiElementWidget<StatusGuiElement> {

    public StatusGuiElementWidget(StatusGuiElement element, IMachineScreen screen) {
        super(element, screen, Component.literal("Status"));
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
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
        ClientHandler.blit(graphics, texture, this.getX(), this.getY(), this.width, this.height);
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
                    tooltips.add(Component.empty().append(Component.literal("✓ ").withStyle(ChatFormatting.GREEN)).append(Component.literal("Core " + index + ": ")).append(Component.translatable("custommachinery.craftingstatus.running")));
                else if(core.getStatus() == MachineStatus.IDLE)
                    tooltips.add(Component.empty().append(Component.literal("|| ").withStyle(ChatFormatting.GOLD)).append(Component.literal("Core " + index + ": ")).append(Component.translatable("custommachinery.craftingstatus.idle")));
                if(core.getStatus() == MachineStatus.ERRORED) {
                    tooltips.add(Component.empty().append(Component.literal("X ").withStyle(ChatFormatting.DARK_RED)).append(Component.literal("Core " + index + ": ")).append(Component.translatable("custommachinery.craftingstatus.errored")));
                    if(core.getError() != null)
                        tooltips.add(Component.literal("  - ").append(core.getError()));
                }
            });
        } else {
            tooltips.add(Component.translatable("custommachinery.craftingstatus." + this.getScreen().getTile().getStatus().toString().toLowerCase(Locale.ROOT)));
            if(this.getScreen().getTile().getStatus() == MachineStatus.ERRORED)
                tooltips.add(this.getScreen().getTile().getMessage());
        }
        return tooltips;
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return false;
    }
}

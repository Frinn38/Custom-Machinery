package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.client.IMachineScreen;
import fr.frinn.custommachinery.client.render.ProgressArrowRenderer;
import fr.frinn.custommachinery.common.crafting.machine.MachineProcessor;
import fr.frinn.custommachinery.common.crafting.machine.MachineProcessorCore;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement;
import fr.frinn.custommachinery.impl.client.AbstractGuiElementRenderable;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ProgressGuiElementWidget extends AbstractGuiElementRenderable<ProgressBarGuiElement> {

    public ProgressGuiElementWidget(ProgressBarGuiElement element, IMachineScreen screen) {
        super(element, screen);
    }

    @Override
    public void extractRenderableRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        ProgressArrowRenderer.renderProgressArrow(graphics, this.getElement(), this.getElement().getX(), this.getElement().getY(), this.getRecipeProgressPercent());
    }

    public double getRecipeProgressPercent() {
        if(this.getScreen().getTile().getProcessor() instanceof MachineProcessor machineProcessor && machineProcessor.getCores().size() > this.getElement().getCore()) {
            MachineProcessorCore core = machineProcessor.getCores().get(this.getElement().getCore());
            if(core.getRecipeTotalTime() == 0)
                return 0;
            return core.getRecipeProgressTime() / core.getRecipeTotalTime();
        }
        else if(this.getScreen().getTile().getMachine().isDummy())
            return (System.currentTimeMillis() % 2000) / 2000.0D;
        else
            return 0;
    }
}

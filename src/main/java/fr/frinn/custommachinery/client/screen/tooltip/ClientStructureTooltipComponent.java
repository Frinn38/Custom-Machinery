package fr.frinn.custommachinery.client.screen.tooltip;

import fr.frinn.custommachinery.api.machine.ICustomMachine;
import fr.frinn.custommachinery.client.screen.pip.StructurePIPRenderer.StructureRenderState;
import fr.frinn.custommachinery.common.util.BlockStructure;
import fr.frinn.custommachinery.common.util.tooltip.StructureTooltipComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public class ClientStructureTooltipComponent implements ClientTooltipComponent {

    private final ICustomMachine machine;
    private final BlockStructure structure;

    public ClientStructureTooltipComponent(StructureTooltipComponent component) {
        this.machine = component.machine();
        this.structure = component.structure();
    }

    @Override
    public int getHeight(Font font) {
        return 80;
    }

    @Override
    public int getWidth(Font font) {
        return 80;
    }

    @Override
    public void extractText(GuiGraphicsExtractor graphics, Font font, int x, int y) {

    }

    @Override
    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
        graphics.submitPictureInPictureRenderState(new StructureRenderState(x, x + width, y, y + 80, graphics.pose(), null, this.structure, this.machine));
    }
}

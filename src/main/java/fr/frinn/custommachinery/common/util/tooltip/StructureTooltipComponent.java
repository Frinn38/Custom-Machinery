package fr.frinn.custommachinery.common.util.tooltip;

import fr.frinn.custommachinery.api.machine.ICustomMachine;
import fr.frinn.custommachinery.common.util.BlockStructure;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record StructureTooltipComponent(ICustomMachine machine, BlockStructure structure) implements TooltipComponent {

}

package fr.frinn.custommachinery.common.util.tooltip;

import fr.frinn.custommachinery.common.machine.CustomMachine;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record MachineTooltipComponent(CustomMachine machine) implements TooltipComponent {

}

package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.block.pattern.BlockPattern;

import javax.annotation.Nullable;

public class StructureMachineComponent extends AbstractMachineComponent {

    public StructureMachineComponent(MachineComponentManager manager) {
        super(manager, Mode.NONE);
    }

    @Override
    public MachineComponentType<StructureMachineComponent> getType() {
        return Registration.STRUCTURE_MACHINE_COMPONENT.get();
    }

    @Nullable
    public BlockPattern.PatternHelper checkStructure(BlockPattern pattern) {
        if(getManager().getTile().getWorld() == null)
            return null;
        return pattern.match(getManager().getTile().getWorld(), getManager().getTile().getPos());
    }
}

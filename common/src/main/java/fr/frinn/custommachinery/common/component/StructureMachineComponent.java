package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.BlockStructure;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class StructureMachineComponent extends AbstractMachineComponent {

    public StructureMachineComponent(IMachineComponentManager manager) {
        super(manager, ComponentIOMode.NONE);
    }

    @Override
    public MachineComponentType<StructureMachineComponent> getType() {
        return Registration.STRUCTURE_MACHINE_COMPONENT.get();
    }

    public boolean checkStructure(BlockStructure pattern) {
        return pattern.match(getManager().getTile().getLevel(), getManager().getTile().getBlockPos(), getManager().getTile().getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
    }

    public void destroyStructure(BlockStructure pattern, boolean drops) {
        pattern.getBlocks(getManager().getTile().getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)).forEach((pos, ingredient) -> {
            if(!ingredient.test(PartialBlockState.MACHINE))
                getManager().getLevel().destroyBlock(pos.offset(getManager().getTile().getBlockPos()), drops);
        });
    }
}

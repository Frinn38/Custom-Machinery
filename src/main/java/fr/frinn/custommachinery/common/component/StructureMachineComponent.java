package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.BlockIngredient;
import fr.frinn.custommachinery.common.util.BlockStructure;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

public class StructureMachineComponent extends AbstractMachineComponent {

    public StructureMachineComponent(IMachineComponentManager manager) {
        super(manager, ComponentIOMode.NONE);
    }

    @Override
    public MachineComponentType<StructureMachineComponent> getType() {
        return Registration.STRUCTURE_MACHINE_COMPONENT.get();
    }

    public boolean checkStructure(BlockStructure pattern) {
        return pattern.match(getManager().getLevel(), getManager().getTile().getBlockPos(), getManager().getTile().getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
    }

    public void destroyStructure(BlockStructure pattern, boolean drops) {
        pattern.getBlocks(getManager().getTile().getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)).forEach((pos, ingredients) -> {
            if(ingredients.stream().noneMatch(blockIngredient -> blockIngredient != BlockIngredient.MACHINE) && ingredients.stream().noneMatch(blockIngredient -> blockIngredient != BlockIngredient.ANY))
                getManager().getLevel().destroyBlock(pos.offset(getManager().getTile().getBlockPos()), drops);
        });
    }

    public void placeStructure(BlockStructure pattern, boolean drops) {
        pattern.getBlocks(getManager().getTile().getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)).forEach((pos, ingredients) -> {
            BlockPos worldPos = pos.offset(getManager().getTile().getBlockPos());
            List<BlockIngredient> validToPlace = ingredients.stream().filter(blockIngredient -> !blockIngredient.not() && blockIngredient != BlockIngredient.ANY && blockIngredient != BlockIngredient.MACHINE).toList();
            if(pos != BlockPos.ZERO && !validToPlace.isEmpty() && validToPlace.stream().noneMatch(blockIngredient -> blockIngredient.test(new BlockInWorld(getManager().getLevel(), worldPos, false)))) {
                if(!getManager().getLevel().getBlockState(worldPos).isAir())
                    getManager().getLevel().destroyBlock(worldPos, drops);
                setBlock(getManager().getLevel(), worldPos, validToPlace.stream().flatMap(blockIngredient -> blockIngredient.getAll().stream()).findAny().orElse(PartialBlockState.AIR));
            }
        });
    }

    private void setBlock(Level world, BlockPos pos, PartialBlockState state) {
        world.setBlockAndUpdate(pos, state.getBlockState());
        BlockEntity tile = world.getBlockEntity(pos);
        if(tile != null && state.getNbt() != null && !state.getNbt().isEmpty()) {
            CompoundTag nbt = state.getNbt().copy();
            nbt.putInt("x", pos.getX());
            nbt.putInt("y", pos.getY());
            nbt.putInt("z", pos.getZ());
            tile.loadWithComponents(nbt, world.registryAccess());
        }
    }
}

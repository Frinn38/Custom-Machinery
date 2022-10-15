package fr.frinn.custommachinery.forge.integration.buildinggadgets;

import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileDataSerializer;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileEntityData;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class CustomMachineTileData implements ITileEntityData {

    private final ResourceLocation machineID;

    public CustomMachineTileData(ResourceLocation machineID) {
        this.machineID = machineID;
    }

    public ResourceLocation getMachineID() {
        return this.machineID;
    }

    @Override
    public ITileDataSerializer getSerializer() {
        return BuildingGadgetsIntegration.MACHINE_TILE_DATA_SERIALIZER.get();
    }

    @Override
    public boolean placeIn(BuildContext context, BlockState state, BlockPos pos) {
        LevelAccessor world = context.getWorld();

        world.setBlock(pos, state, Block.UPDATE_ALL);
        Optional.ofNullable(world.getBlockEntity(pos))
                .filter(tile -> tile instanceof CustomMachineTile)
                .map(tile -> (CustomMachineTile)tile)
                .ifPresent(machine -> machine.setId(this.machineID));

        return true;
    }
}

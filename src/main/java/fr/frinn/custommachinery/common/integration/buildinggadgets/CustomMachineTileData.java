package fr.frinn.custommachinery.common.integration.buildinggadgets;

import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileDataSerializer;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileEntityData;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.util.Constants;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
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
        IWorld world = context.getWorld();

        world.setBlockState(pos, state, Constants.BlockFlags.DEFAULT_AND_RERENDER);
        Optional.ofNullable(world.getTileEntity(pos))
                .filter(tile -> tile instanceof CustomMachineTile)
                .map(tile -> (CustomMachineTile)tile)
                .ifPresent(machine -> machine.setId(machineID));

        return true;
    }
}

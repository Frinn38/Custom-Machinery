package fr.frinn.custommachinery.forge.integration.buildinggadgets;

import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileDataFactory;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileEntityData;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

public class CustomMachineTileDataFactory implements ITileDataFactory {

    @Nullable
    @Override
    public ITileEntityData createDataFor(BlockEntity tile) {
        if(tile instanceof CustomMachineTile && tile.getType() == Registration.CUSTOM_MACHINE_TILE.get())
            return new CustomMachineTileData(((CustomMachineTile)tile).getId());
        return null;
    }
}

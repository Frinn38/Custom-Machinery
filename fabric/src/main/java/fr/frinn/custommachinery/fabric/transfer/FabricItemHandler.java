package fr.frinn.custommachinery.fabric.transfer;

import com.google.common.collect.Maps;
import fr.frinn.custommachinery.common.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.transfer.ICommonItemHandler;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideMode;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class FabricItemHandler implements ICommonItemHandler {

    private final ItemComponentHandler handler;
    private final Map<Direction, SidedItemStorage> sidedStorages = Maps.newEnumMap(Direction.class);
    private final Map<Direction, BlockApiCache<Storage<ItemVariant>, Direction>> neighbourStorages = Maps.newEnumMap(Direction.class);


    public FabricItemHandler(ItemComponentHandler handler) {
        this.handler = handler;
        for(Direction side : Direction.values())
            this.sidedStorages.put(side, new SidedItemStorage(handler, side));
    }

    public Storage<ItemVariant> getItemStorage(Direction side) {
        return this.sidedStorages.get(side);
    }

    @Override
    public void configChanged(RelativeSide side, SideMode oldMode, SideMode newMode) {
        if(oldMode.isNone() != newMode.isNone())
            this.handler.getManager().getLevel().updateNeighborsAt(this.handler.getManager().getTile().getBlockPos(), Registration.CUSTOM_MACHINE_BLOCK.get());
    }

    @Override
    public void invalidate() {

    }

    @Override
    public void tick() {
        for(Direction side : Direction.values()) {
            if(this.handler.getComponents().stream().allMatch(component -> component.getConfig().getSideMode(side) == SideMode.NONE))
                continue;

            if(this.neighbourStorages.get(side) == null)
                this.neighbourStorages.put(side, BlockApiCache.create(ItemStorage.SIDED, (ServerLevel)this.handler.getManager().getLevel(), this.handler.getManager().getTile().getBlockPos().relative(side)));

            Storage<ItemVariant> neighbour = this.neighbourStorages.get(side).find(side.getOpposite());

            if(neighbour == null)
                continue;

            for(ItemSlot tank : this.sidedStorages.get(side).parts) {
                if(tank.getComponent().getConfig().isAutoInput() && tank.getComponent().getConfig().getSideMode(side).isInput() && tank.getAmount() < tank.getCapacity())
                    StorageUtil.move(neighbour, tank, fluid -> true, Long.MAX_VALUE, null);

                if(tank.getComponent().getConfig().isAutoOutput() && tank.getComponent().getConfig().getSideMode(side).isOutput() && tank.getAmount() > 0)
                    StorageUtil.move(this.sidedStorages.get(side), neighbour, fluid -> true, Long.MAX_VALUE, null);
            }
        }
    }
}

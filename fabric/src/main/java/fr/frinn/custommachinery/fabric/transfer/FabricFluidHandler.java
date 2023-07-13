package fr.frinn.custommachinery.fabric.transfer;

import com.google.common.collect.Maps;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.util.transfer.ICommonFluidHandler;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideMode;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class FabricFluidHandler implements ICommonFluidHandler {

    private final FluidComponentHandler fluidHandler;
    private final Storage<FluidVariant> generalTank;
    private final Map<Direction, SidedFluidStorage> sidedTanks = Maps.newEnumMap(Direction.class);
    private final Map<Direction, BlockApiCache<Storage<FluidVariant>, Direction>> neighbourStorages = Maps.newEnumMap(Direction.class);

    public FabricFluidHandler(FluidComponentHandler fluidHandler) {
        this.fluidHandler = fluidHandler;
        this.generalTank = new SidedFluidStorage(fluidHandler, null);
        for(Direction side : Direction.values())
            this.sidedTanks.put(side, new SidedFluidStorage(fluidHandler, side));
    }

    @Override
    public void configChanged(RelativeSide side, SideMode oldMode, SideMode newMode) {
        if(oldMode.isNone() != newMode.isNone())
            this.fluidHandler.getManager().getLevel().updateNeighborsAt(this.fluidHandler.getManager().getTile().getBlockPos(), this.fluidHandler.getManager().getTile().getBlockState().getBlock());
    }

    public Storage<FluidVariant> getFluidStorage(Direction side) {
        return this.sidedTanks.get(side);
    }

    @Override
    public void invalidate() {

    }

    @Override
    public void tick() {
        for(Direction side : Direction.values()) {
            if(this.fluidHandler.getComponents().stream().allMatch(component -> component.getConfig().getSideMode(side) == SideMode.NONE))
                continue;

            if(this.neighbourStorages.get(side) == null)
                this.neighbourStorages.put(side, BlockApiCache.create(FluidStorage.SIDED, (ServerLevel)this.fluidHandler.getManager().getLevel(), this.fluidHandler.getManager().getTile().getBlockPos().relative(side)));

            Storage<FluidVariant> neighbour = this.neighbourStorages.get(side).find(side.getOpposite());

            if(neighbour == null)
                continue;

            for(FluidTank tank : this.sidedTanks.get(side).parts) {
                if(tank.getComponent().getConfig().isAutoInput() && tank.getComponent().getConfig().getSideMode(side).isInput() && tank.getAmount() < tank.getCapacity())
                    StorageUtil.move(neighbour, tank, fluid -> true, Long.MAX_VALUE, null);

                if(tank.getComponent().getConfig().isAutoOutput() && tank.getComponent().getConfig().getSideMode(side).isOutput() && tank.getAmount() > 0)
                    StorageUtil.move(this.sidedTanks.get(side), neighbour, fluid -> true, Long.MAX_VALUE, null);
            }
        }
    }

    @Override
    public boolean interactWithFluidHandler(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if(stack.isEmpty())
            return false;

        Storage<FluidVariant> storage = FluidStorage.ITEM.find(stack, ContainerItemContext.ofPlayerHand(player, hand));
        if(storage == null)
            return false;

        long filled = StorageUtil.move(storage, this.generalTank, fluid -> true, Long.MAX_VALUE, null);
        if(filled > 0)
            return true;
        return StorageUtil.move(this.generalTank, storage, fluid -> true, Long.MAX_VALUE, null) > 0;
    }
}

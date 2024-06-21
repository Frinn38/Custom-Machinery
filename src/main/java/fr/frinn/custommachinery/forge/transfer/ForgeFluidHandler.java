package fr.frinn.custommachinery.forge.transfer;

import com.google.common.collect.Maps;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.util.transfer.ICommonFluidHandler;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideMode;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ForgeFluidHandler implements ICommonFluidHandler {

    private final FluidComponentHandler fluidHandler;

    private final IFluidHandler generalHandler;
    private final Map<Direction, SidedFluidStorage> sidedStorages = Maps.newEnumMap(Direction.class);
    private final Map<Direction, BlockCapabilityCache<IFluidHandler, Direction>> neighbourStorages = Maps.newEnumMap(Direction.class);
    private final InteractionFluidStorage interactionFluidStorage;

    public ForgeFluidHandler(FluidComponentHandler fluidHandler) {
        this.fluidHandler = fluidHandler;
        this.generalHandler = new SidedFluidStorage(null, fluidHandler);
        for(Direction direction : Direction.values()) {
            SidedFluidStorage storage = new SidedFluidStorage(direction, fluidHandler);
            this.sidedStorages.put(direction, storage);
        }
        this.interactionFluidStorage = new InteractionFluidStorage(this.fluidHandler);
    }

    @Nullable
    public IFluidHandler getCapability(@Nullable Direction side) {
        if(side == null)
            return this.generalHandler;
        else if(this.fluidHandler.getComponents().stream().anyMatch(component -> !component.getConfig().getSideMode(side).isNone()))
            return this.sidedStorages.get(side);
        else
            return null;
    }

    @Override
    public void configChanged(RelativeSide side, SideMode oldMode, SideMode newMode) {
        if(oldMode.isNone() != newMode.isNone())
            this.fluidHandler.getManager().getLevel().updateNeighborsAt(this.fluidHandler.getManager().getTile().getBlockPos(), this.fluidHandler.getManager().getTile().getBlockState().getBlock());
    }

    @Override
    public void invalidate() {
        this.fluidHandler.getManager().getTile().invalidateCapabilities();
    }

    @Override
    public void tick() {
        //I/O between the machine and neighbour blocks.
        for(Direction side : Direction.values()) {
            if(this.fluidHandler.getComponents().stream().allMatch(component -> component.getConfig().getSideMode(side) == SideMode.NONE))
                continue;

            IFluidHandler neighbour;

            if(this.neighbourStorages.get(side) == null) {
                this.neighbourStorages.put(side, BlockCapabilityCache.create(FluidHandler.BLOCK, (ServerLevel)this.fluidHandler.getManager().getLevel(), this.fluidHandler.getManager().getTile().getBlockPos().relative(side), side.getOpposite(), () -> !this.fluidHandler.getManager().getTile().isRemoved(), () -> this.neighbourStorages.remove(side)));
                if(this.neighbourStorages.get(side) != null)
                    neighbour = this.neighbourStorages.get(side).getCapability();
                else
                    continue;
            }
            else
                neighbour = this.neighbourStorages.get(side).getCapability();

            if(neighbour == null)
                continue;

            this.fluidHandler.getComponents().forEach(component -> {
                if(component.getConfig().isAutoInput() && component.getConfig().getSideMode(side).isInput() && component.getFluidStack().getAmount() < component.getCapacity())
                    FluidUtil.tryFluidTransfer(this.sidedStorages.get(side), neighbour, Integer.MAX_VALUE, true);

                if(component.getConfig().isAutoOutput() && component.getConfig().getSideMode(side).isOutput() && component.getFluidStack().getAmount() > 0)
                    FluidUtil.tryFluidTransfer(neighbour, this.sidedStorages.get(side), Integer.MAX_VALUE, true);
            });
        }
    }

    /** Right click with fluid handler compatibility **/
    @Override
    public boolean interactWithFluidHandler(Player player, InteractionHand hand) {
        return FluidUtil.interactWithFluidHandler(player, hand, this.interactionFluidStorage);
    }
}

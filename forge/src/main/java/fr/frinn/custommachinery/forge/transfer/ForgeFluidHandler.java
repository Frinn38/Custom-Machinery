package fr.frinn.custommachinery.forge.transfer;

import com.google.common.collect.Maps;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.transfer.ICommonFluidHandler;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideMode;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

public class ForgeFluidHandler implements ICommonFluidHandler {

    private final FluidComponentHandler fluidHandler;

    private final IFluidHandler generalHandler;
    private final LazyOptional<IFluidHandler> capability;
    private final Map<Direction, SidedFluidStorage> sidedStorages = Maps.newEnumMap(Direction.class);
    private final Map<Direction, LazyOptional<IFluidHandler>> sidedWrappers = Maps.newEnumMap(Direction.class);
    private final Map<Direction, LazyOptional<IFluidHandler>> neighbourStorages = Maps.newEnumMap(Direction.class);
    private final InteractionFluidStorage interactionFluidStorage;

    public ForgeFluidHandler(FluidComponentHandler fluidHandler) {
        this.fluidHandler = fluidHandler;
        this.generalHandler = new SidedFluidStorage(null, fluidHandler);
        this.capability = LazyOptional.of(() -> this.generalHandler);
        for(Direction direction : Direction.values()) {
            SidedFluidStorage storage = new SidedFluidStorage(direction, fluidHandler);
            this.sidedStorages.put(direction, storage);
            this.sidedWrappers.put(direction, LazyOptional.of(() -> storage));
        }
        this.interactionFluidStorage = new InteractionFluidStorage(this.fluidHandler);
    }

    public <T> LazyOptional<T> getCapability(@Nullable Direction side) {
        if(side == null)
            return this.capability.cast();
        else if(this.fluidHandler.getComponents().stream().anyMatch(component -> !component.getConfig().getSideMode(side).isNone()))
            return this.sidedWrappers.get(side).cast();
        else
            return LazyOptional.empty();
    }

    @Override
    public void configChanged(RelativeSide side, SideMode oldMode, SideMode newMode) {
        if(oldMode.isNone() != newMode.isNone())
            this.fluidHandler.getManager().getLevel().updateNeighborsAt(this.fluidHandler.getManager().getTile().getBlockPos(), Registration.CUSTOM_MACHINE_BLOCK.get());
    }

    @Override
    public void invalidate() {
        this.capability.invalidate();
        this.sidedWrappers.values().forEach(LazyOptional::invalidate);
    }

    @Override
    public void tick() {
        //I/O between the machine and neighbour blocks.
        for(Direction side : Direction.values()) {
            if(this.fluidHandler.getComponents().stream().allMatch(component -> component.getConfig().getSideMode(side) == SideMode.NONE))
                continue;

            LazyOptional<IFluidHandler> neighbour;

            if(this.neighbourStorages.get(side) == null) {
                neighbour = Optional.ofNullable(this.fluidHandler.getManager().getLevel().getBlockEntity(this.fluidHandler.getManager().getTile().getBlockPos().relative(side))).map(tile -> tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite())).orElse(LazyOptional.empty());
                neighbour.ifPresent(storage -> {
                    neighbour.addListener(cap -> this.neighbourStorages.remove(side));
                    this.neighbourStorages.put(side, neighbour);
                });
            }
            else
                neighbour = this.neighbourStorages.get(side);

            neighbour.ifPresent(storage -> {
                this.fluidHandler.getComponents().forEach(component -> {
                    if(component.getConfig().isAutoInput() && component.getConfig().getSideMode(side).isInput() && component.getFluidStack().getAmount() < component.getCapacity())
                        FluidUtil.tryFluidTransfer(this.sidedStorages.get(side), storage, Integer.MAX_VALUE, true);

                    if(component.getConfig().isAutoOutput() && component.getConfig().getSideMode(side).isOutput() && component.getFluidStack().getAmount() > 0)
                        FluidUtil.tryFluidTransfer(storage, this.sidedStorages.get(side), Integer.MAX_VALUE, true);
                });
            });
        }
    }

    /** Right click with fluid handler compatibility **/
    @Override
    public boolean interactWithFluidHandler(Player player, InteractionHand hand) {
        return FluidUtil.interactWithFluidHandler(player, hand, this.interactionFluidStorage);
    }
}

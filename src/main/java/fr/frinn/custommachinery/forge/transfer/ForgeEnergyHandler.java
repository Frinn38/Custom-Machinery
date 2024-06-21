package fr.frinn.custommachinery.forge.transfer;

import com.google.common.collect.Maps;
import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.util.transfer.ICommonEnergyHandler;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideMode;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ForgeEnergyHandler implements ICommonEnergyHandler {

    private final EnergyMachineComponent component;
    private final SidedEnergyStorage generalStorage;
    private final Map<Direction, SidedEnergyStorage> sidedStorages = Maps.newEnumMap(Direction.class);
    private final Map<Direction, BlockCapabilityCache<IEnergyStorage, Direction>> neighbourStorages = Maps.newEnumMap(Direction.class);

    public ForgeEnergyHandler(EnergyMachineComponent component) {
        this.component = component;
        this.generalStorage = new SidedEnergyStorage(() -> SideMode.BOTH, component);
        for(Direction direction : Direction.values()) {
            SidedEnergyStorage storage = new SidedEnergyStorage(() -> component.getConfig().getSideMode(direction), component);
            this.sidedStorages.put(direction, storage);
        }
    }

    @Override
    public void configChanged(RelativeSide side, SideMode oldMode, SideMode newMode) {
        if(oldMode.isNone() != newMode.isNone()) {
            if(oldMode.isNone())
                this.component.getManager().getLevel().updateNeighborsAt(this.component.getManager().getTile().getBlockPos(), this.component.getManager().getTile().getBlockState().getBlock());
        }
    }

    @Override
    public void invalidate() {
        this.component.getManager().getTile().invalidateCapabilities();
    }

    @Override
    public void tick() {
        for(Direction side : Direction.values()) {
            if(this.component.getConfig().getSideMode(side) == SideMode.NONE)
                continue;

            IEnergyStorage neighbour;

            if(this.neighbourStorages.get(side) == null) {
                this.neighbourStorages.put(side, BlockCapabilityCache.create(EnergyStorage.BLOCK, (ServerLevel)this.component.getManager().getLevel(), this.component.getManager().getTile().getBlockPos().relative(side), side.getOpposite(), () -> !this.component.getManager().getTile().isRemoved(), () -> this.neighbourStorages.remove(side)));
                if(this.neighbourStorages.get(side) != null)
                    neighbour = this.neighbourStorages.get(side).getCapability();
                else
                    continue;
            }
            else
                neighbour = this.neighbourStorages.get(side).getCapability();

            if(neighbour == null)
                continue;

            if(this.component.getConfig().isAutoInput() && this.component.getConfig().getSideMode(side).isInput() && this.component.getEnergy() < this.component.getCapacity())
                move(neighbour, this.sidedStorages.get(side), Integer.MAX_VALUE);

            if(this.component.getConfig().isAutoOutput() && this.component.getConfig().getSideMode(side).isOutput() && this.component.getEnergy() > 0)
                move(this.sidedStorages.get(side), neighbour, Integer.MAX_VALUE);
        }
    }

    @Nullable
    public IEnergyStorage getCapability(@Nullable Direction side) {
        if(side == null)
            return this.generalStorage;
        else if(this.component.getConfig().getSideMode(side).isInput() || this.component.getConfig().getSideMode(side).isOutput())
            return this.sidedStorages.get(side);
        else
            return null;
    }

    private void move(IEnergyStorage from, IEnergyStorage to, int maxAmount) {
        int maxExtracted = from.extractEnergy(maxAmount, true);
        if(maxExtracted > 0) {
            int maxInserted = to.receiveEnergy(maxExtracted, true);
            if(maxInserted > 0) {
                from.extractEnergy(maxInserted, false);
                to.receiveEnergy(maxExtracted, false);
            }
        }
    }
}

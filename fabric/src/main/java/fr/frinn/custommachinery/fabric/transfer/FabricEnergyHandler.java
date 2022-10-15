package fr.frinn.custommachinery.fabric.transfer;

import com.google.common.collect.Maps;
import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.transfer.ICommonEnergyHandler;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideMode;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;

import java.util.Map;

public class FabricEnergyHandler implements ICommonEnergyHandler {

    private final EnergyMachineComponent component;
    private final Map<Direction, EnergyStorage> sidedStorages = Maps.newEnumMap(Direction.class);
    private final Map<Direction, BlockApiCache<EnergyStorage, Direction>> neighbourStorages = Maps.newEnumMap(Direction.class);

    public FabricEnergyHandler(EnergyMachineComponent component) {
        this.component = component;
        for (Direction side : Direction.values()) {
            this.sidedStorages.put(side, new SidedEnergyStorage(side, component));
        }
    }

    public EnergyStorage getStorage(Direction side) {
        return this.sidedStorages.get(side);
    }

    @Override
    public void configChanged(RelativeSide side, SideMode oldMode, SideMode newMode) {
        if(oldMode.isNone() != newMode.isNone())
            this.component.getManager().getLevel().updateNeighborsAt(this.component.getManager().getTile().getBlockPos(), Registration.CUSTOM_MACHINE_BLOCK.get());
    }

    @Override
    public void invalidate() {

    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void tick() {
        for(Direction side : Direction.values()) {
            if(this.component.getConfig().getSideMode(side) == SideMode.NONE)
                continue;

            if(this.neighbourStorages.get(side) == null)
                this.neighbourStorages.put(side, BlockApiCache.create(EnergyStorage.SIDED, (ServerLevel)this.component.getManager().getLevel(), this.component.getManager().getTile().getBlockPos().relative(side)));

            EnergyStorage neighbour = this.neighbourStorages.get(side).find(side.getOpposite());

            if(neighbour == null)
                continue;

            if(this.component.getConfig().isAutoInput() && this.component.getConfig().getSideMode(side).isInput() && this.component.getEnergy() < this.component.getCapacity())
                EnergyStorageUtil.move(neighbour, this.sidedStorages.get(side), Long.MAX_VALUE, null);

            if(this.component.getConfig().isAutoOutput() && this.component.getConfig().getSideMode(side).isOutput() && this.component.getEnergy() > 0)
                EnergyStorageUtil.move(this.sidedStorages.get(side), neighbour, Long.MAX_VALUE, null);
        }
    }
}

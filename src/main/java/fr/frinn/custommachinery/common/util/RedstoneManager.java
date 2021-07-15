package fr.frinn.custommachinery.common.util;

import fr.frinn.custommachinery.common.data.component.RedstoneMachineComponent;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.Direction;

import java.util.stream.Stream;

public class RedstoneManager {

    private CustomMachineTile tile;

    public RedstoneManager(CustomMachineTile tile) {
        this.tile = tile;
    }

    public int getComparatorInput() {
        return this.tile.componentManager.getComponent(Registration.REDSTONE_MACHINE_COMPONENT.get()).map(RedstoneMachineComponent::getComparatorInput).orElse(0);
    }

    public int getPowerOutput() {
        return this.tile.componentManager.getComponent(Registration.REDSTONE_MACHINE_COMPONENT.get()).map(RedstoneMachineComponent::getPowerOutput).orElse(0);
    }

    public void tick() {
        if(!this.tile.isPaused() && this.shouldPauseMachine())
            this.tile.setPaused(true);
        if(this.tile.isPaused() && !this.shouldPauseMachine())
            this.tile.setPaused(false);
    }

    private boolean shouldPauseMachine() {
        if(this.tile.getWorld() == null)
            return false;
        return Stream.of(Direction.values()).mapToInt(direction -> this.tile.getWorld().getRedstonePower(this.tile.getPos(), direction)).max().orElse(0) >=
                this.tile.componentManager.getComponent(Registration.REDSTONE_MACHINE_COMPONENT.get()).map(RedstoneMachineComponent::getPowerToPause).orElse(1);
    }
}

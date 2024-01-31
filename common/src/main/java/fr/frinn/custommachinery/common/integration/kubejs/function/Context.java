package fr.frinn.custommachinery.common.integration.kubejs.function;

import dev.latvian.mods.kubejs.level.BlockContainerJS;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import net.minecraft.network.chat.Component;

public class Context {

    private final ICraftingContext internal;
    private final MachineJS machine;

    public Context(ICraftingContext internal) {
        this.internal = internal;
        this.machine = new MachineJS((CustomMachineTile) getTile());
    }

    public double getRemainingTime() {
        return this.internal.getRemainingTime();
    }

    public double getBaseSpeed() {
        return this.internal.getBaseSpeed();
    }

    public void setBaseSpeed(double baseSpeed) {
        this.internal.setBaseSpeed(baseSpeed);
    }

    public double getModifiedSpeed() {
        return this.internal.getModifiedSpeed();
    }

    public MachineTile getTile() {
        return this.internal.getMachineTile();
    }

    public MachineJS getMachine() {
        return this.machine;
    }

    public BlockContainerJS getBlock() {
        return new BlockContainerJS(getTile().getLevel(), getTile().getBlockPos());
    }

    public Result success() {
        return Result.success();
    }

    public Result error(Component error) {
        return Result.error(error);
    }
}

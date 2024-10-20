package fr.frinn.custommachinery.common.integration.kubejs.function;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.level.BlockContainerJS;
import dev.latvian.mods.rhino.Context;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.common.init.CustomMachineTile;

public class FunctionKubeEvent implements KubeEvent {

    private final ICraftingContext internal;
    private final MachineJS machine;

    public FunctionKubeEvent(ICraftingContext internal) {
        this.internal = internal;
        this.machine = new MachineJS((CustomMachineTile) getTile());
    }

    public FunctionKubeEvent getContext() {
        return this;
    }

    public FunctionKubeEvent getCtx() {
        return this;
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

    @Override
    public Object defaultExitValue(Context cx) {
        return CraftingResult.pass();
    }
}

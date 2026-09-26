package fr.frinn.custommachinery.common.integration.kubejs.function;

import dev.latvian.mods.kubejs.event.EventExit;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.level.CachedLevelBlock;
import dev.latvian.mods.rhino.Context;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.machine.MachineTile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class FunctionKubeEvent implements KubeEvent {

    private final ICraftingContext internal;
    private final MachineJS machine;

    public FunctionKubeEvent(ICraftingContext internal) {
        this.internal = internal;
        this.machine = new MachineJS(internal.getMachineTile());
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

    public CachedLevelBlock getBlock() {
        return new CachedLevelBlock(getTile().getComponentManager().getLevel(), getTile().getBlockPos());
    }

    public IMachineRecipe getRecipe() {
        return this.internal.getRecipe();
    }

    public Identifier getRecipeId() {
        return this.internal.getRecipeId();
    }

    @Override
    public Object defaultExitValue(Context cx) {
        return CraftingResult.pass();
    }

    public void error(Context cx, Component error) throws EventExit {
        this.cancel(cx, error);
    }
}

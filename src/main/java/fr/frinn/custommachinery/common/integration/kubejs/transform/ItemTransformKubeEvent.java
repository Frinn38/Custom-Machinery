package fr.frinn.custommachinery.common.integration.kubejs.transform;

import dev.latvian.mods.kubejs.event.EventExit;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.level.CachedLevelBlock;
import dev.latvian.mods.rhino.Context;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.common.integration.kubejs.function.MachineJS;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ItemTransformKubeEvent implements KubeEvent {

    private final ICraftingContext internal;
    private final MachineJS machine;
    private final ItemStack input;
    private final ItemStack originalOutput;

    public ItemTransformKubeEvent(ICraftingContext internal, ItemStack input, ItemStack originalOutput) {
        this.internal = internal;
        this.machine = MachineJS.of(getTile());
        this.input = input;
        this.originalOutput = originalOutput;
    }

    public ItemTransformKubeEvent getContext() {
        return this;
    }

    public ItemTransformKubeEvent getCtx() {
        return this;
    }

    public MachineTile getTile() {
        return this.internal.getMachineTile();
    }

    public MachineJS getMachine() {
        return this.machine;
    }

    public CachedLevelBlock getBlock() {
        return new CachedLevelBlock(getTile().getLevel(), getTile().getBlockPos());
    }

    public IMachineRecipe getRecipe() {
        return this.internal.getRecipe();
    }

    public ResourceLocation getRecipeId() {
        return this.internal.getRecipeId();
    }

    public ItemStack getInput() {
        return this.input;
    }

    public ItemStack getOriginalOutput() {
        return this.originalOutput;
    }

    @Override
    public Object defaultExitValue(Context cx) {
        return CraftingResult.pass();
    }

    public void error(Context cx, Component error) throws EventExit {
        this.cancel(cx, error);
    }
}

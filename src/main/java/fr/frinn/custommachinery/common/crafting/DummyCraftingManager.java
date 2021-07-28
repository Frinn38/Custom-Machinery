package fr.frinn.custommachinery.common.crafting;

import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.common.crafting.CraftingManager;
import fr.frinn.custommachinery.common.crafting.CustomMachineRecipe;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.function.Consumer;

public class DummyCraftingManager extends CraftingManager {

    public DummyCraftingManager(CustomMachineTile tile) {
        super(tile);
    }

    @Override
    public void tick() {

    }

    @Override
    public ITextComponent getErrorMessage() {
        return StringTextComponent.EMPTY;
    }

    @Override
    public void setIdle() {

    }

    @Override
    public void setErrored(ITextComponent message) {

    }

    @Override
    public void setRunning() {

    }

    @Override
    public STATUS getStatus() {
        return STATUS.IDLE;
    }

    @Override
    public CustomMachineRecipe getCurrentRecipe() {
        return null;
    }

    @Override
    public void addProbeInfo(IProbeInfo info) {

    }

    @Override
    public CompoundNBT serializeNBT() {
        return new CompoundNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {

    }

    @Override
    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {

    }
}

package fr.frinn.custommachinery.common.crafting;

import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.function.Consumer;

public class DummyCraftingManager extends CraftingManager {

    public DummyCraftingManager(CustomMachineTile tile) {
        super(tile);
    }

    @Override
    public void tick() {

    }

    @Override
    public Component getErrorMessage() {
        return TextComponent.EMPTY;
    }

    @Override
    public void setStatus(MachineStatus status, Component mesage) {

    }

    @Override
    public MachineStatus getStatus() {
        return MachineStatus.IDLE;
    }

    @Override
    public CustomMachineRecipe getCurrentRecipe() {
        return null;
    }

    @Override
    public void addProbeInfo(IProbeInfo info) {

    }

    @Override
    public CompoundTag serializeNBT() {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

    }

    @Override
    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {

    }
}

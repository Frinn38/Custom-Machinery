package fr.frinn.custommachinery.forge.init;

import fr.frinn.custommachinery.common.component.MachineComponentManager;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.forge.client.CustomMachineBakedModel;
import fr.frinn.custommachinery.forge.transfer.ForgeEnergyHandler;
import fr.frinn.custommachinery.forge.transfer.ForgeFluidHandler;
import fr.frinn.custommachinery.forge.transfer.ForgeItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ForgeCustomMachineTile extends CustomMachineTile {

    public ForgeCustomMachineTile(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public void refreshClientData() {
        requestModelDataUpdate();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @NotNull
    @Override
    public ModelData getModelData() {
        return ModelData.builder()
                .with(CustomMachineBakedModel.APPEARANCE, getAppearance().copy())
                .with(CustomMachineBakedModel.STATUS, getStatus())
                .build();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        MachineComponentManager manager = this.getComponentManager();

        if(capability == ForgeCapabilities.ENERGY)
            return manager.getComponent(Registration.ENERGY_MACHINE_COMPONENT.get())
                    .map(energy -> ((ForgeEnergyHandler)energy.getEnergyHandler()).getCapability(side))
                    .orElse(LazyOptional.empty())
                    .cast();
        else if(capability == ForgeCapabilities.FLUID_HANDLER)
            return manager.getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                    .map(handler -> ((FluidComponentHandler)handler).getCommonFluidHandler())
                    .map(handler -> ((ForgeFluidHandler)handler).getCapability(side))
                    .orElse(LazyOptional.empty())
                    .cast();
        else if(capability == ForgeCapabilities.ITEM_HANDLER)
            return manager.getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                    .map(handler -> ((ItemComponentHandler)handler).getCommonHandler())
                    .map(handler -> ((ForgeItemHandler)handler).getCapability(side))
                    .orElse(LazyOptional.empty())
                    .cast();

        return super.getCapability(capability, side);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        super.unload();
    }
}

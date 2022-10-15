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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
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

    @NotNull
    @Override
    public IModelData getModelData() {
        return new ModelDataMap.Builder()
                .withInitial(CustomMachineBakedModel.APPEARANCE, getMachine().getAppearance(getStatus()).copy())
                .withInitial(CustomMachineBakedModel.STATUS, getStatus())
                .build();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        MachineComponentManager manager = this.componentManager;

        if(capability == CapabilityEnergy.ENERGY)
            return manager.getComponent(Registration.ENERGY_MACHINE_COMPONENT.get())
                    .map(energy -> ((ForgeEnergyHandler)energy.getEnergyHandler()).getCapability(side))
                    .orElse(LazyOptional.empty())
                    .cast();
        else if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return manager.getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                    .map(handler -> ((FluidComponentHandler)handler).getCommonFluidHandler())
                    .map(handler -> ((ForgeFluidHandler)handler).getCapability(side))
                    .orElse(LazyOptional.empty())
                    .cast();
        else if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return manager.getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                    .map(handler -> ((ItemComponentHandler)handler).getCommonHandler())
                    .map(handler -> ((ForgeItemHandler)handler).getCapability(side))
                    .orElse(LazyOptional.empty())
                    .cast();

        return super.getCapability(capability, side);
    }
}

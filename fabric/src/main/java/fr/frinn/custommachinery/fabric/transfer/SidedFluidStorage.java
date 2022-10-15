package fr.frinn.custommachinery.fabric.transfer;

import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class SidedFluidStorage extends CombinedStorage<FluidVariant, FluidTank> {

    private final FluidComponentHandler fluidHandler;
    @Nullable
    private final Direction side;

    public SidedFluidStorage(FluidComponentHandler fluidHandler, @Nullable Direction side) {
        super(fluidHandler.getComponents().stream().map(component -> new FluidTank(component, side)).toList());
        this.fluidHandler = fluidHandler;
        this.side = side;
    }
}

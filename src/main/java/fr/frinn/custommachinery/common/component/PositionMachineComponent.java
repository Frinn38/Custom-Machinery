package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.apiimpl.component.AbstractMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public class PositionMachineComponent extends AbstractMachineComponent {

    public PositionMachineComponent(IMachineComponentManager manager) {
        super(manager, ComponentIOMode.NONE);
    }

    @Override
    public MachineComponentType<PositionMachineComponent> getType() {
        return Registration.POSITION_MACHINE_COMPONENT.get();
    }

    public BlockPos getPosition() {
        return this.getManager().getTile().getBlockPos();
    }

    public Biome getBiome() {
        return this.getManager().getLevel().getBiome(getPosition()).value();
    }

    public ResourceKey<Level> getDimension() {
        return getManager().getLevel().dimension();
    }
}

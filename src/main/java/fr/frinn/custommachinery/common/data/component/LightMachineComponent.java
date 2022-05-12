package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.ITickableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.apiimpl.component.AbstractMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.world.level.LightLayer;

public class LightMachineComponent extends AbstractMachineComponent implements ITickableComponent {

    private boolean emmitLight;

    public LightMachineComponent(IMachineComponentManager manager) {
        super(manager, ComponentIOMode.BOTH);
    }

    @Override
    public MachineComponentType<LightMachineComponent> getType() {
        return Registration.LIGHT_MACHINE_COMPONENT.get();
    }

    @Override
    public void clientTick() {
        if(getManager().getTile().getMachine().getAppearance(getManager().getTile().getStatus()).getLightLevel() > 0 != this.emmitLight) {
            this.emmitLight = !this.emmitLight;
            getManager().getWorld().getChunkSource().getLightEngine().checkBlock(getManager().getTile().getBlockPos());
        }
    }

    public int getMachineLight() {
        if(this.emmitLight)
            return getManager().getTile().getMachine().getAppearance(getManager().getTile().getStatus()).getLightLevel();
        return 0;
    }

    public int getSkyLight() {
        return getManager().getWorld().getBrightness(LightLayer.SKY, getManager().getTile().getBlockPos());
    }

    public int getBlockLight() {
        return getManager().getWorld().getBrightness(LightLayer.BLOCK, getManager().getTile().getBlockPos());
    }
}

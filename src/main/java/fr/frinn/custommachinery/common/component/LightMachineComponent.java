package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.ITickableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import net.minecraft.world.level.LightLayer;

public class LightMachineComponent extends AbstractMachineComponent implements ITickableComponent {

    private boolean emmitLight = false;

    public LightMachineComponent(IMachineComponentManager manager) {
        super(manager, ComponentIOMode.BOTH);
    }

    @Override
    public MachineComponentType<LightMachineComponent> getType() {
        return Registration.LIGHT_MACHINE_COMPONENT.get();
    }

    @Override
    public void clientTick() {
        if(getManager().getLevel().getGameTime() % 20 == 0) {
            this.emmitLight = getManager().getTile().getAppearance().getLightLevel() > 0;
            getManager().getLevel().getLightEngine().checkBlock(getManager().getTile().getBlockPos());
        }
    }

    public int getMachineLight() {
        if(this.emmitLight)
            return getManager().getTile().getAppearance().getLightLevel();
        return 0;
    }

    public int getSkyLight() {
        return getManager().getLevel().getBrightness(LightLayer.SKY, getManager().getTile().getBlockPos());
    }

    public int getBlockLight() {
        return getManager().getLevel().getBrightness(LightLayer.BLOCK, getManager().getTile().getBlockPos());
    }
}

package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.api.components.ComponentIOMode;
import fr.frinn.custommachinery.api.components.IMachineComponentManager;
import fr.frinn.custommachinery.api.components.ITickableComponent;
import fr.frinn.custommachinery.api.components.MachineComponentType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import net.minecraft.world.LightType;

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
        if(getManager().getTile().getWorld() == null)
            return;

        if(getManager().getTile().getMachine().getAppearance(getManager().getTile().getStatus()).getLightLevel() > 0 != this.emmitLight) {
            this.emmitLight = !this.emmitLight;
            getManager().getTile().getWorld().getChunkProvider().getLightManager().checkBlock(getManager().getTile().getPos());
        }
    }

    public int getMachineLight() {
        if(this.emmitLight)
            return getManager().getTile().getMachine().getAppearance(getManager().getTile().getStatus()).getLightLevel();
        return 0;
    }

    public int getSkyLight() {
        if(getManager().getTile().getWorld() != null)
            return getManager().getTile().getWorld().getLightFor(LightType.SKY, getManager().getTile().getPos());
        return 0;
    }

    public int getBlockLight() {
        if(getManager().getTile().getWorld() != null)
            return getManager().getTile().getWorld().getLightFor(LightType.BLOCK, getManager().getTile().getPos());
        return 0;
    }
}

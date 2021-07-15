package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.api.components.ComponentIOMode;
import fr.frinn.custommachinery.api.components.IMachineComponentManager;
import fr.frinn.custommachinery.api.components.MachineComponentType;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.world.LightType;

public class LightMachineComponent extends AbstractMachineComponent {

    public LightMachineComponent(IMachineComponentManager manager) {
        super(manager, ComponentIOMode.BOTH);
    }

    @Override
    public MachineComponentType<LightMachineComponent> getType() {
        return Registration.LIGHT_MACHINE_COMPONENT.get();
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

package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.api.components.ComponentIOMode;
import fr.frinn.custommachinery.api.components.IMachineComponentManager;
import fr.frinn.custommachinery.api.components.ITickableComponent;
import fr.frinn.custommachinery.api.components.MachineComponentType;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.data.MachineAppearance;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
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

        boolean shouldEmmit = getAppearance().getLightMode().toString().equalsIgnoreCase(((CustomMachineTile)getManager().getTile()).craftingManager.getStatus().toString());
        if((getAppearance().getLightMode() == MachineAppearance.LightMode.ALWAYS || shouldEmmit) != this.emmitLight) {
            this.emmitLight = !this.emmitLight;
            getManager().getTile().getWorld().getChunkProvider().getLightManager().checkBlock(getManager().getTile().getPos());
        }
    }

    public int getMachineLight() {
        if(this.emmitLight)
            return getAppearance().getLightLevel();
        return 0;
    }

    private MachineAppearance getAppearance() {
        return ((CustomMachine)getManager().getTile().getMachine()).getAppearance();
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

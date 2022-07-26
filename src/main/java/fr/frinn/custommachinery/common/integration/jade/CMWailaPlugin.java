package fr.frinn.custommachinery.common.integration.jade;

import fr.frinn.custommachinery.common.init.CustomMachineBlock;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;

@WailaPlugin
public class CMWailaPlugin implements IWailaPlugin {

    @Override
    public void register(IRegistrar registrar) {
        registrar.registerComponentProvider(CustomMachineComponentProvider.INSTANCE, TooltipPosition.BODY, CustomMachineBlock.class);
        registrar.registerComponentProvider(CustomMachineComponentProvider.INSTANCE, TooltipPosition.HEAD, CustomMachineBlock.class);
        registrar.registerBlockDataProvider(CustomMachineServerDataProvider.INSTANCE, CustomMachineBlock.class);
    }
}

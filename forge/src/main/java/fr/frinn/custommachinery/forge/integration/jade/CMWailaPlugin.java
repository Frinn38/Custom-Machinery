package fr.frinn.custommachinery.forge.integration.jade;

import fr.frinn.custommachinery.common.init.CustomMachineBlock;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import mcp.mobius.waila.api.IWailaClientRegistration;
import mcp.mobius.waila.api.IWailaCommonRegistration;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;

@WailaPlugin
public class CMWailaPlugin implements IWailaPlugin {

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerComponentProvider(CustomMachineComponentProvider.INSTANCE, TooltipPosition.BODY, CustomMachineBlock.class);
        registration.usePickedResult(Registration.CUSTOM_MACHINE_BLOCK.get());
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(CustomMachineServerDataProvider.INSTANCE, CustomMachineTile.class);
    }
}
package fr.frinn.custommachinery.common.integration.jade;

import fr.frinn.custommachinery.common.init.Registration;
import mcp.mobius.waila.api.IWailaClientRegistration;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.WailaPlugin;

@WailaPlugin
public class CMWailaPlugin implements IWailaPlugin {

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.usePickedResult(Registration.CUSTOM_MACHINE_BLOCK.get());
    }
}
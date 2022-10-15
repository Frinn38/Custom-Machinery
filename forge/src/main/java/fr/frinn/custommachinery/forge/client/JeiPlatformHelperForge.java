package fr.frinn.custommachinery.forge.client;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.forge.FluidStackHooksForge;
import fr.frinn.custommachinery.client.integration.jei.IJeiPlatformHelper;
import mezz.jei.api.helpers.IJeiHelpers;

public class JeiPlatformHelperForge implements IJeiPlatformHelper {

    public Object convertFluidStack(FluidStack stack, IJeiHelpers helpers) {
        return FluidStackHooksForge.toForge(stack);
    }
}

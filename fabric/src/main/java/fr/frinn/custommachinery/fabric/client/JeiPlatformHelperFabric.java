package fr.frinn.custommachinery.fabric.client;

import dev.architectury.fluid.FluidStack;
import fr.frinn.custommachinery.client.integration.jei.IJeiPlatformHelper;
import mezz.jei.api.helpers.IJeiHelpers;

public class JeiPlatformHelperFabric implements IJeiPlatformHelper {

    public Object convertFluidStack(FluidStack stack, IJeiHelpers helpers) {
        return helpers.getPlatformFluidHelper().create(stack.getFluid(), stack.getAmount(), stack.getTag());
    }
}

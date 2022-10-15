package fr.frinn.custommachinery.client.integration.jei;

import dev.architectury.fluid.FluidStack;
import dev.architectury.platform.Platform;
import mezz.jei.api.helpers.IJeiHelpers;
import net.minecraft.Util;

public interface IJeiPlatformHelper {

    IJeiPlatformHelper INSTANCE = Util.make(() -> {
        try {
            Class<?> clazz = Class.forName(Platform.isForge() ? "fr.frinn.custommachinery.forge.client.JeiPlatformHelperForge" : "fr.frinn.custommachinery.fabric.client.JeiPlatformHelperFabric");
            return (IJeiPlatformHelper) clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JEI platform helper");
        }
    });

    Object convertFluidStack(FluidStack stack, IJeiHelpers helpers);
}

package fr.frinn.custommachinery.common.util;

import fr.frinn.custommachinery.client.ModelHandle;
import fr.frinn.custommachinery.common.data.MachineAppearance;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.model.data.EmptyModelData;

public class ClientUtils {

    public static void setParticleTexture(MachineAppearance appearance) {
        net.minecraft.client.renderer.texture.TextureAtlasSprite particleTexture;
        if(appearance.getType() == MachineAppearance.AppearanceType.BLOCKSTATE)
            particleTexture = Minecraft.getInstance().getModelManager().getModel(appearance.getBlockstate()).getParticleTexture(EmptyModelData.INSTANCE);
        else if(appearance.getType() == MachineAppearance.AppearanceType.BLOCK)
            particleTexture = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(appearance.getBlock().getDefaultState()).getParticleTexture(EmptyModelData.INSTANCE);
        else if(appearance.getType() == MachineAppearance.AppearanceType.MODEL)
            particleTexture = new ModelHandle(appearance.getModel()).getParticleTexture();
        else
            particleTexture = Minecraft.getInstance().getModelManager().getMissingModel().getParticleTexture(EmptyModelData.INSTANCE);
        net.minecraft.client.renderer.model.IBakedModel dummyModel = new fr.frinn.custommachinery.common.util.DummyBakedModel(particleTexture);
        Registration.CUSTOM_MACHINE_BLOCK.get().getStateContainer().getValidStates().forEach(state ->
                Minecraft.getInstance().getModelManager().getBlockModelShapes().bakedModelStore.replace(state, dummyModel)
        );
    }
}

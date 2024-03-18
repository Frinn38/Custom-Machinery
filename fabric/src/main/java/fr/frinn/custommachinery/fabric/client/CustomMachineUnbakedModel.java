package fr.frinn.custommachinery.fabric.client;

import fr.frinn.custommachinery.api.machine.MachineStatus;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

public class CustomMachineUnbakedModel implements UnbakedModel {

    private final Map<MachineStatus, ResourceLocation> defaults;

    public CustomMachineUnbakedModel(Map<MachineStatus, ResourceLocation> defaults) {
        this.defaults = defaults;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return this.defaults.values();
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver) {

    }

    @Nullable
    @Override
    public BakedModel bake(ModelBaker modelBaker, Function<Material, TextureAtlasSprite> function, ModelState modelState, ResourceLocation resourceLocation) {
        return new CustomMachineBakedModel(this.defaults);
    }
}

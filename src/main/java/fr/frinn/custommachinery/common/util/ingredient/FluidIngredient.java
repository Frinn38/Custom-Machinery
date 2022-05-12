package fr.frinn.custommachinery.common.util.ingredient;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.common.util.Codecs;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class FluidIngredient implements IIngredient<Fluid> {

    private static final Codec<FluidIngredient> CODEC_FOR_DATAPACK = ForgeRegistries.FLUIDS.getCodec().xmap(FluidIngredient::new, ingredient -> ingredient.fluid);
    private static final Codec<FluidIngredient> CODEC_FOR_KUBEJS = ForgeRegistries.FLUIDS.getCodec().fieldOf("fluid").codec().xmap(FluidIngredient::new, ingredient -> ingredient.fluid);
    public static final Codec<FluidIngredient> CODEC = Codecs.either(CODEC_FOR_DATAPACK, CODEC_FOR_KUBEJS, "Fluid Ingredient")
            .xmap(either -> either.map(Function.identity(), Function.identity()), Either::left);

    private final Fluid fluid;

    public FluidIngredient(Fluid fluid) {
        this.fluid = fluid;
    }

    @Override
    public List<Fluid> getAll() {
        return Collections.singletonList(this.fluid);
    }

    @Override
    public boolean test(Fluid fluid) {
        return this.fluid == fluid;
    }

    @Override
    public String toString() {
        return this.fluid.getRegistryName().toString();
    }
}

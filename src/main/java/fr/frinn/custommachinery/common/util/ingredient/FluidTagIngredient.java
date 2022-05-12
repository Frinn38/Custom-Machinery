package fr.frinn.custommachinery.common.util.ingredient;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.TagUtil;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import java.util.List;
import java.util.function.Function;

public class FluidTagIngredient implements IIngredient<Fluid> {

    private static final Codec<FluidTagIngredient> CODEC_FOR_DATAPACK = TagKey.codec(Registry.FLUID_REGISTRY).xmap(FluidTagIngredient::new, ingredient -> ingredient.tag);
    private static final Codec<FluidTagIngredient> CODEC_FOR_KUBEJS = TagKey.codec(Registry.FLUID_REGISTRY).fieldOf("tag").codec().xmap(FluidTagIngredient::new, ingredient -> ingredient.tag);
    public static final Codec<FluidTagIngredient> CODEC = Codecs.either(CODEC_FOR_DATAPACK, CODEC_FOR_KUBEJS, "Fluid Tag Ingredient")
            .xmap(either -> either.map(Function.identity(), Function.identity()), Either::left);

    private final TagKey<Fluid> tag;

    private FluidTagIngredient(TagKey<Fluid> tag) {
        this.tag = tag;
    }

    public  static FluidTagIngredient create(String s) throws IllegalArgumentException {
        if(s.startsWith("#"))
            s = s.substring(1);
        if(!Utils.isResourceNameValid(s))
            throw new IllegalArgumentException(String.format("Invalid tag id : %s", s));
        TagKey<Fluid> tag = TagKey.create(Registry.FLUID_REGISTRY, new ResourceLocation(s));
        return new FluidTagIngredient(tag);
    }

    public static FluidTagIngredient create(TagKey<Fluid> tag) throws IllegalArgumentException {
        return new FluidTagIngredient(tag);
    }

    @Override
    public List<Fluid> getAll() {
        return TagUtil.getFluids(this.tag).toList();
    }

    @Override
    public boolean test(Fluid fluid) {
        return this.getAll().contains(fluid);
    }

    @Override
    public String toString() {
        return "#" + this.tag.location();
    }
}

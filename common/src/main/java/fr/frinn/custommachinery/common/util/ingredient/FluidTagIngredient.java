package fr.frinn.custommachinery.common.util.ingredient;

import com.mojang.datafixers.util.Either;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.TagUtil;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import java.util.List;
import java.util.function.Function;

public class FluidTagIngredient implements IIngredient<Fluid> {

    private static final NamedCodec<FluidTagIngredient> CODEC_FOR_DATAPACK = NamedCodec.STRING.xmap(FluidTagIngredient::create, FluidTagIngredient::toString, "Fluid tag ingredient");
    private static final NamedCodec<FluidTagIngredient> CODEC_FOR_KUBEJS = DefaultCodecs.tagKey(Registry.FLUID_REGISTRY).fieldOf("tag").xmap(FluidTagIngredient::new, ingredient -> ingredient.tag, "Fluid tag ingredient");
    public static final NamedCodec<FluidTagIngredient> CODEC = Codecs.either(CODEC_FOR_DATAPACK, CODEC_FOR_KUBEJS, "Fluid Tag Ingredient")
            .xmap(either -> either.map(Function.identity(), Function.identity()), Either::left, "Fluid tag ingredient");

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

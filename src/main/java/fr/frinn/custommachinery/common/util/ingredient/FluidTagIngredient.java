package fr.frinn.custommachinery.common.util.ingredient;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.common.util.TagUtil;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import java.util.List;
import java.util.function.Function;

public class FluidTagIngredient implements IIngredient<Fluid> {

    private static final NamedCodec<FluidTagIngredient> CODEC_FOR_DATAPACK = NamedCodec.STRING.comapFlatMap(string -> {
        try {
            return DataResult.success(FluidTagIngredient.create(string));
        } catch (IllegalArgumentException e) {
            return DataResult.error(e::getMessage);
        }
    }, FluidTagIngredient::toString, "Fluid tag ingredient");
    private static final NamedCodec<FluidTagIngredient> CODEC_FOR_KUBEJS = DefaultCodecs.tagKey(Registries.FLUID).fieldOf("tag").xmap(FluidTagIngredient::new, ingredient -> ingredient.tag, "Fluid tag ingredient");
    public static final NamedCodec<FluidTagIngredient> CODEC = NamedCodec.either(CODEC_FOR_DATAPACK, CODEC_FOR_KUBEJS, "Fluid Tag Ingredient")
            .xmap(either -> either.map(Function.identity(), Function.identity()), Either::left, "Fluid tag ingredient");

    private final TagKey<Fluid> tag;

    private FluidTagIngredient(TagKey<Fluid> tag) {
        this.tag = tag;
    }

    public static FluidTagIngredient create(String s) throws IllegalArgumentException {
        if(s.startsWith("#"))
            s = s.substring(1);
        if(!Utils.isResourceNameValid(s))
            throw new IllegalArgumentException(String.format("Invalid tag id : %s", s));
        TagKey<Fluid> tag = TagKey.create(Registries.FLUID, ResourceLocation.parse(s));
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

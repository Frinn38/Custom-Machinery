package fr.frinn.custommachinery.common.util.ingredient;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.fluid.Fluid;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.function.Function;

public class FluidTagIngredient implements IIngredient<Fluid> {

    private static final Codec<FluidTagIngredient> CODEC_FOR_DATAPACK = Codecs.FLUID_TAG_CODEC.xmap(FluidTagIngredient::new, ingredient -> ingredient.tag);
    private static final Codec<FluidTagIngredient> CODEC_FOR_KUBEJS = Codecs.FLUID_TAG_CODEC.fieldOf("tag").codec().xmap(FluidTagIngredient::new, ingredient -> ingredient.tag);
    public static final Codec<FluidTagIngredient> CODEC = Codecs.either(CODEC_FOR_DATAPACK, CODEC_FOR_KUBEJS, "Fluid Tag Ingredient")
            .xmap(either -> either.map(Function.identity(), Function.identity()), Either::left);

    private ITag<Fluid> tag;

    public FluidTagIngredient(ITag<Fluid> tag) {
        this.tag = tag;
    }

    public FluidTagIngredient(String s) {
        if(s.startsWith("#"))
            s = s.substring(1);
        if(!Utils.isResourceNameValid(s))
            throw new IllegalArgumentException(String.format("Invalid tag id : %s", s));
        this.tag = TagCollectionManager.getManager().getFluidTags().get(new ResourceLocation(s));
    }

    public FluidTagIngredient(ResourceLocation loc) {
        this.tag = TagCollectionManager.getManager().getFluidTags().get(loc);
    }

    @Override
    public List<Fluid> getAll() {
        return this.tag.getAllElements();
    }

    @Override
    public boolean test(Fluid fluid) {
        return this.tag.contains(fluid);
    }

    @Override
    public String toString() {
        return "#" + TagCollectionManager.getManager().getFluidTags().getDirectIdFromTag(this.tag);
    }
}

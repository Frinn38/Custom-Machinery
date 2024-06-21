package fr.frinn.custommachinery.common.util.ingredient;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.DataResult;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import fr.frinn.custommachinery.common.util.TagUtil;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BlockTagIngredient implements IIngredient<PartialBlockState> {

    public static final NamedCodec<BlockTagIngredient> CODEC = NamedCodec.STRING.comapFlatMap(string -> {
        try {
            return DataResult.success(BlockTagIngredient.create(string));
        } catch (IllegalArgumentException e) {
            return DataResult.error(e::getMessage);
        }
    }, BlockTagIngredient::toString, "Block tag ingredient");

    private final TagKey<Block> tag;
    private final Supplier<List<PartialBlockState>> ingredients;

    private BlockTagIngredient(TagKey<Block> tag) {
        this.tag = tag;
        this.ingredients = Suppliers.memoize(() -> TagUtil.getBlocks(this.tag).map(PartialBlockState::new).collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf)));
    }

    public static BlockTagIngredient create(String s) throws IllegalArgumentException {
        if(s.startsWith("#"))
            s = s.substring(1);
        if(!Utils.isResourceNameValid(s))
            throw new IllegalArgumentException(String.format("Invalid tag id : %s", s));
        TagKey<Block> tag = TagKey.create(Registries.BLOCK, ResourceLocation.parse(s));
        return new BlockTagIngredient(tag);
    }

    @Override
    public List<PartialBlockState> getAll() {
        return this.ingredients.get();
    }

    @Override
    public boolean test(PartialBlockState partialBlockState) {
        return this.ingredients.get().stream().anyMatch(state -> state.getBlockState() == partialBlockState.getBlockState());
    }

    @Override
    public String toString() {
        return "#" + this.tag.location();
    }
}

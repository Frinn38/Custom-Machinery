package fr.frinn.custommachinery.common.util.ingredient;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import net.minecraft.block.Block;
import net.minecraft.tags.ITag;
import net.minecraftforge.common.util.Lazy;

import java.util.List;
import java.util.stream.Collectors;

public class BlockTagIngredient implements IIngredient<PartialBlockState> {

    public static final Codec<BlockTagIngredient> CODEC = Codecs.BLOCK_TAG_CODEC.xmap(BlockTagIngredient::new, ingredient -> ingredient.tag);

    private ITag.INamedTag<Block> tag;
    private Lazy<List<PartialBlockState>> ingredients;

    public BlockTagIngredient(ITag.INamedTag<Block> tag) {
        this.tag = tag;
        this.ingredients = Lazy.of(() -> tag.getAllElements().stream().map(PartialBlockState::new).collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf)));
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
        return "#" + this.tag.getName();
    }
}

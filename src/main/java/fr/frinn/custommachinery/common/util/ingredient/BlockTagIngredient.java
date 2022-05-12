package fr.frinn.custommachinery.common.util.ingredient;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import fr.frinn.custommachinery.common.util.TagUtil;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.Lazy;

import java.util.List;
import java.util.stream.Collectors;

public class BlockTagIngredient implements IIngredient<PartialBlockState> {

    public static final Codec<BlockTagIngredient> CODEC = TagKey.codec(Registry.BLOCK_REGISTRY).xmap(BlockTagIngredient::new, ingredient -> ingredient.tag);

    private final TagKey<Block> tag;
    private final Lazy<List<PartialBlockState>> ingredients;

    private BlockTagIngredient(TagKey<Block> tag) {
        this.tag = tag;
        this.ingredients = Lazy.of(() -> TagUtil.getBlocks(this.tag).map(PartialBlockState::new).collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf)));
    }

    public BlockTagIngredient create(TagKey<Block> tag) {
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

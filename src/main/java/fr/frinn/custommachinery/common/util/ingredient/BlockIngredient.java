package fr.frinn.custommachinery.common.util.ingredient;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.PartialBlockState;

import java.util.Collections;
import java.util.List;

public class BlockIngredient implements IIngredient<PartialBlockState> {

    public static final BlockIngredient AIR = new BlockIngredient(PartialBlockState.AIR);
    public static final BlockIngredient ANY = new BlockIngredient(PartialBlockState.ANY);
    public static final Codec<BlockIngredient> CODEC = Codecs.PARTIAL_BLOCK_STATE_CODEC.xmap(BlockIngredient::new, ingredient -> ingredient.partialBlockState);

    private final PartialBlockState partialBlockState;

    public BlockIngredient(PartialBlockState partialBlockState) {
        this.partialBlockState = partialBlockState;
    }

    @Override
    public List<PartialBlockState> getAll() {
        return Collections.singletonList(this.partialBlockState);
    }

    @Override
    public boolean test(PartialBlockState partialBlockState) {
        return this.partialBlockState.getBlockState() == partialBlockState.getBlockState();
    }

    @Override
    public String toString() {
        return this.partialBlockState.toString();
    }
}

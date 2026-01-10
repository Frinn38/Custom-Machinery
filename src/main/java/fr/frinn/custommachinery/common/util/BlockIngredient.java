package fr.frinn.custommachinery.common.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public record BlockIngredient(boolean not, @Nullable PartialBlockState state, @Nullable TagKey<Block> tag) {

    public static final NamedCodec<BlockIngredient> MAP_CODEC = NamedCodec.record(blockIngredientInstance ->
                    blockIngredientInstance.group(
                            NamedCodec.BOOL.optionalFieldOf("not", false).forGetter(ingredient -> ingredient.not),
                            PartialBlockState.CODEC.optionalFieldOf("state").forGetter(ingredient -> Optional.ofNullable(ingredient.state)),
                            DefaultCodecs.tagKey(Registries.BLOCK).optionalFieldOf("tag").forGetter(ingredient -> Optional.ofNullable(ingredient.tag))
                    ).apply(blockIngredientInstance, (not, state, tag) -> new BlockIngredient(not, state.orElse(null), tag.orElse(null))),
            "Block ingredient"
    );

    public static final NamedCodec<BlockIngredient> STRING_CODEC = NamedCodec.STRING.comapFlatMap(s -> {
        try {
            return DataResult.success(BlockIngredient.of(s));
        } catch (CommandSyntaxException e) {
            return DataResult.error(e::getMessage);
        }
    }, BlockIngredient::serialize, "Block ingredient");

    public static final NamedCodec<BlockIngredient> CODEC = NamedCodec.either(MAP_CODEC, STRING_CODEC).xmap(either -> either.map(Function.identity(), Function.identity()), Either::left, "Block ingredient");

    public static final BlockIngredient AIR = new BlockIngredient(false, PartialBlockState.AIR, null);
    public static final BlockIngredient ANY = new BlockIngredient(false, PartialBlockState.ANY, null);
    public static final BlockIngredient MACHINE = new BlockIngredient(false, PartialBlockState.MACHINE, null);

    public static BlockIngredient of(CharSequence s) throws CommandSyntaxException {
        StringReader reader = new StringReader(s.toString());
        reader.skipWhitespace();

        boolean not = false;
        if (reader.peek() == '!') {
            not = true;
            reader.skip();
        }

        if (reader.peek() == '#') {
            reader.skip();
            TagKey<Block> tag = TagKey.create(Registries.BLOCK, ResourceLocation.read(reader));
            return new BlockIngredient(not, null, tag);
        }

        PartialBlockState state = PartialBlockState.of(reader.getRemaining());
        return new BlockIngredient(not, state, null);
    }

    public boolean test(BlockInWorld block) {
        if (this.state != null) {
            if (this.not)
                return !this.state.test(block);
            else
                return this.state.test(block);
        }

        if (this.tag != null) {
            boolean valid = block.getState().is(this.tag);
            if (this.not)
                return !valid;
            else
                return valid;
        }

        return false;
    }

    public boolean test(Block block) {
        if (this.state != null) {
            if (this.not)
                return this.state.getBlockState().getBlock() != block;
            else
                return this.state.getBlockState().getBlock() == block;
        }

        if (this.tag != null) {
            boolean valid = block.defaultBlockState().is(this.tag);
            if (this.not)
                return !valid;
            else
                return valid;
        }

        return false;
    }

    public BlockIngredient rotate(Rotation rotation) {
        if (this.state != null)
            return new BlockIngredient(this.not, this.state.rotate(rotation), this.tag);
        return this;
    }

    public List<PartialBlockState> getAll() {
        if(this.state != null)
            return List.of(this.state);

        if(this.tag != null)
            return BuiltInRegistries.BLOCK.getTag(this.tag).map(named -> named.stream().map(holder -> new PartialBlockState(holder.value())).toList()).orElse(Collections.emptyList());

        return Collections.emptyList();
    }

    public String serialize() {
        StringBuilder builder = new StringBuilder();
        if(this.not)
            builder.append('!');
        if(this.state != null)
            return builder.append(BlockStateParser.serialize(this.state.getBlockState())).toString();
        if(this.tag != null)
            return builder.append('#').append(this.tag.location()).toString();
        return "ERROR";
    }

    @Override
    public String toString() {
        if(this.state != null) {
            if(this.not)
                return "!" + this.state;
            else
                return this.state.toString();
        }

        if(this.tag != null) {
            if(this.not)
                return "!#" + this.tag.location();
            else
                return "#" + this.tag.location();
        }

        return "ERROR";
    }
}

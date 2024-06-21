package fr.frinn.custommachinery.common.crafting.craft;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CustomCraftRecipeSerializer implements RecipeSerializer<CustomCraftRecipe> {

    @Override
    public MapCodec<CustomCraftRecipe> codec() {
        return CustomCraftRecipeBuilder.CODEC.mapCodec().xmap(CustomCraftRecipeBuilder::build, CustomCraftRecipeBuilder::new);
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, CustomCraftRecipe> streamCodec() {
        return ByteBufCodecs.fromCodecWithRegistries(codec().codec());
    }
}

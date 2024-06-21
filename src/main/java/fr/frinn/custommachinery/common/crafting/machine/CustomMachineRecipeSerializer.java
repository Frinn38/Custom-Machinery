package fr.frinn.custommachinery.common.crafting.machine;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CustomMachineRecipeSerializer implements RecipeSerializer<CustomMachineRecipe> {

    @Override
    public MapCodec<CustomMachineRecipe> codec() {
        return CustomMachineRecipeBuilder.CODEC.mapCodec().xmap(CustomMachineRecipeBuilder::build, CustomMachineRecipeBuilder::new);
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, CustomMachineRecipe> streamCodec() {
        return ByteBufCodecs.fromCodecWithRegistries(codec().codec());
    }
}

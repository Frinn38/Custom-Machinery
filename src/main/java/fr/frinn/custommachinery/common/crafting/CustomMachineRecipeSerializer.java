package fr.frinn.custommachinery.common.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.CustomMachinery;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;

public class CustomMachineRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<CustomMachineRecipe> {

    @ParametersAreNonnullByDefault
    @Override
    public CustomMachineRecipe read(ResourceLocation recipeId, JsonObject json) {
        DataResult<Pair<CustomMachineRecipeBuilder, JsonElement>> result = CustomMachineRecipeBuilder.CODEC.decode(JsonOps.INSTANCE, json);
        CustomMachineRecipeBuilder builder = result.resultOrPartial(CustomMachinery.LOGGER::error).orElseThrow(() -> new JsonParseException("Error while reading recipe: " + recipeId + " from json")).getFirst();
        return builder.build(recipeId);
    }

    @ParametersAreNonnullByDefault
    @Nullable
    @Override
    public CustomMachineRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
        try {
            return buffer.func_240628_a_(CustomMachineRecipeBuilder.CODEC).build(recipeId);
        } catch (IOException e) {
            RuntimeException exception = new RuntimeException("Error while reading recipe: " + recipeId + " from packet !");
            exception.setStackTrace(e.getStackTrace());
            throw exception;
        }
    }

    @ParametersAreNonnullByDefault
    @Override
    public void write(PacketBuffer buffer, CustomMachineRecipe recipe) {
        try {
            buffer.func_240629_a_(CustomMachineRecipeBuilder.CODEC, new CustomMachineRecipeBuilder(recipe));
        } catch (IOException e) {
            RuntimeException exception = new RuntimeException("Error while writing recipe: " + recipe.getId() + " to packet !");
            exception.setStackTrace(e.getStackTrace());
            throw exception;
        }
    }
}

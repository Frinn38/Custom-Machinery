package fr.frinn.custommachinery.common.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class CustomMachineRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<CustomMachineRecipe> {

    public static final Codec<CustomMachineRecipe> CODEC = RecordCodecBuilder.create(recipeBuilderInstance -> recipeBuilderInstance.group(
        ResourceLocation.CODEC.fieldOf("machine").forGetter(CustomMachineRecipe::getMachine),
        Codec.INT.fieldOf("time").forGetter(CustomMachineRecipe::getRecipeTime),
        IRequirement.CODEC.listOf().optionalFieldOf("requirements", new ArrayList<>()).forGetter(CustomMachineRecipe::getRequirements)
    ).apply(recipeBuilderInstance, (machine, time, requirements) -> {
        CustomMachineRecipeBuilder builder = new CustomMachineRecipeBuilder();
        builder.withMachine(machine);
        builder.withTime(time);
        requirements.forEach(builder::withRequirement);
        return builder.build();
    }));

    public static final List<CustomMachineRecipe> RECIPES = new ArrayList<>();

    @ParametersAreNonnullByDefault
    @Override
    public CustomMachineRecipe read(ResourceLocation recipeId, JsonObject json) {
        DataResult<Pair<CustomMachineRecipe, JsonElement>> result = CODEC.decode(JsonOps.INSTANCE, json);
        CustomMachineRecipe recipe = result.resultOrPartial(CustomMachinery.LOGGER::error).orElseThrow(() -> new JsonParseException("Error while reading recipe: " + recipeId + " from json")).getFirst();
        recipe.setId(recipeId);
        RECIPES.add(recipe);
        return recipe;
    }

    @ParametersAreNonnullByDefault
    @Nullable
    @Override
    public CustomMachineRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
        try {
            CustomMachineRecipe recipe = buffer.func_240628_a_(CODEC);
            recipe.setId(recipeId);
            RECIPES.add(recipe);
            return recipe;
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
            buffer.func_240629_a_(CODEC, recipe);
        } catch (IOException e) {
            RuntimeException exception = new RuntimeException("Error while writing recipe: " + recipe.getId() + " to packet !");
            exception.setStackTrace(e.getStackTrace());
            throw exception;
        }
    }
}

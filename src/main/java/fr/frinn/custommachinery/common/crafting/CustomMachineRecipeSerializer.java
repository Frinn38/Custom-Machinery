package fr.frinn.custommachinery.common.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.api.CustomMachineryAPI;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class CustomMachineRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<CustomMachineRecipe> {

    @ParametersAreNonnullByDefault
    @Override
    public CustomMachineRecipe read(ResourceLocation recipeId, JsonObject json) {
        CustomMachineryAPI.info("Parsing recipe json: %s", recipeId);
        DataResult<Pair<CustomMachineRecipeBuilder, JsonElement>> result = CustomMachineRecipeBuilder.CODEC.decode(JsonOps.INSTANCE, json);
        if(result.result().isPresent()) {
            CustomMachineryAPI.info("Successfully read recipe json: %s", recipeId);
            return result.result().get().getFirst().build(recipeId);
        } else if(result.error().isPresent()) {
            CustomMachineryAPI.error("Error while parsing recipe json: %s, skipping...%n%s", recipeId, result.error().get().message());
            throw new JsonParseException("Error while parsing Custom Machine Recipe json: " + recipeId + " error: " + result.error().get().message());
        }
        throw new IllegalStateException("No success nor error when parsing Custom Machine Recipe json: " + recipeId + "This can't happen");
    }

    @ParametersAreNonnullByDefault
    @Nullable
    @Override
    public CustomMachineRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
        CustomMachineryAPI.info("Receiving recipe: %s from server.", recipeId);
        DataResult<CustomMachineRecipeBuilder> result = CustomMachineRecipeBuilder.CODEC.parse(NBTDynamicOps.INSTANCE, buffer.func_244273_m());
        if(result.result().isPresent()) {
            CustomMachineryAPI.info("Sucessfully received recipe: %s from server.", recipeId);
            return result.result().get().build(recipeId);
        } else if(result.error().isPresent()) {
            CustomMachineryAPI.error("Error while parsing recipe json: %s, skipping...%n%s", recipeId, result.error().get().message());
            throw new IllegalArgumentException("Error while receiving Custom Machine Recipe from server: " + recipeId + " error: " + result.error().get().message());
        }
        throw new IllegalStateException("No success nor error when receiving Custom Machine Recipe: " + recipeId + "from server. This can't happen");
    }

    @ParametersAreNonnullByDefault
    @Override
    public void write(PacketBuffer buffer, CustomMachineRecipe recipe) {
        CustomMachineryAPI.info("Sending recipe: %s to clients", recipe.getId());
        DataResult<INBT> result = CustomMachineRecipeBuilder.CODEC.encodeStart(NBTDynamicOps.INSTANCE, new CustomMachineRecipeBuilder(recipe));
        if(result.result().isPresent()) {
            CustomMachineryAPI.info("Sucessfully send recipe: %s to clients.", recipe.getId());
            buffer.writeCompoundTag((CompoundNBT) result.result().get());
            return;
        } else if(result.error().isPresent()) {
            CustomMachineryAPI.error("Error while sending recipe: %s to clients.%n%s", recipe.getId(), result.error().get().message());
            throw new IllegalArgumentException("Error while sending Custom Machine Recipe to clients: " + recipe.getId() + " error: " + result.error().get().message());
        }
        throw new IllegalStateException("No success nor error when sending Custom Machine Recipe: " + recipe.getId() + "to clients. This can't happen");
    }
}

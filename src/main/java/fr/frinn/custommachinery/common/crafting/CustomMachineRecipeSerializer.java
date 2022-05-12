package fr.frinn.custommachinery.common.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class CustomMachineRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<CustomMachineRecipe> {

    @ParametersAreNonnullByDefault
    @Override
    public CustomMachineRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        ICustomMachineryAPI.INSTANCE.logger().info("Parsing recipe json: {}", recipeId);
        DataResult<Pair<CustomMachineRecipeBuilder, JsonElement>> result = CustomMachineRecipeBuilder.CODEC.decode(JsonOps.INSTANCE, json);
        if(result.result().isPresent()) {
            ICustomMachineryAPI.INSTANCE.logger().info("Successfully read recipe json: {}", recipeId);
            return result.result().get().getFirst().build(recipeId);
        } else if(result.error().isPresent()) {
            ICustomMachineryAPI.INSTANCE.logger().error("Error while parsing recipe json: {}, skipping...\n{}", recipeId, result.error().get().message());
            throw new JsonParseException("Error while parsing Custom Machine Recipe json: " + recipeId + " error: " + result.error().get().message());
        }
        throw new IllegalStateException("No success nor error when parsing Custom Machine Recipe json: " + recipeId + "This can't happen");
    }

    @ParametersAreNonnullByDefault
    @Nullable
    @Override
    public CustomMachineRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        ICustomMachineryAPI.INSTANCE.logger().info("Receiving recipe: {} from server.", recipeId);
        DataResult<CustomMachineRecipeBuilder> result = CustomMachineRecipeBuilder.CODEC.parse(NbtOps.INSTANCE, buffer.readAnySizeNbt());
        if(result.result().isPresent()) {
            ICustomMachineryAPI.INSTANCE.logger().info("Sucessfully received recipe: {} from server.", recipeId);
            return result.result().get().build(recipeId);
        } else if(result.error().isPresent()) {
            ICustomMachineryAPI.INSTANCE.logger().error("Error while parsing recipe json: {}, skipping...\n{}", recipeId, result.error().get().message());
            throw new IllegalArgumentException("Error while receiving Custom Machine Recipe from server: " + recipeId + " error: " + result.error().get().message());
        }
        throw new IllegalStateException("No success nor error when receiving Custom Machine Recipe: " + recipeId + "from server. This can't happen");
    }

    @ParametersAreNonnullByDefault
    @Override
    public void toNetwork(FriendlyByteBuf buffer, CustomMachineRecipe recipe) {
        ICustomMachineryAPI.INSTANCE.logger().info("Sending recipe: {} to clients", recipe.getId());
        DataResult<Tag> result = CustomMachineRecipeBuilder.CODEC.encodeStart(NbtOps.INSTANCE, new CustomMachineRecipeBuilder(recipe));
        if(result.result().isPresent()) {
            ICustomMachineryAPI.INSTANCE.logger().info("Sucessfully send recipe: {} to clients.", recipe.getId());
            buffer.writeNbt((CompoundTag) result.result().get());
            return;
        } else if(result.error().isPresent()) {
            ICustomMachineryAPI.INSTANCE.logger().error("Error while sending recipe: {} to clients.%n{}", recipe.getId(), result.error().get().message());
            throw new IllegalArgumentException("Error while sending Custom Machine Recipe to clients: " + recipe.getId() + " error: " + result.error().get().message());
        }
        throw new IllegalStateException("No success nor error when sending Custom Machine Recipe: " + recipe.getId() + "to clients. This can't happen");
    }
}

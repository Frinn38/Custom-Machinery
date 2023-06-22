package fr.frinn.custommachinery.common.crafting.craft;

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
import org.jetbrains.annotations.Nullable;

public class CustomCraftRecipeSerializer implements RecipeSerializer<CustomCraftRecipe> {

    @Override
    public CustomCraftRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        ICustomMachineryAPI.INSTANCE.logger().info("Parsing craft recipe json: {}", recipeId);
        DataResult<Pair<CustomCraftRecipeBuilder, JsonElement>> result = CustomCraftRecipeBuilder.CODEC.decode(JsonOps.INSTANCE, json);
        if(result.result().isPresent()) {
            ICustomMachineryAPI.INSTANCE.logger().info("Successfully read craft recipe json: {}", recipeId);
            return result.result().get().getFirst().build(recipeId);
        } else if(result.error().isPresent()) {
            ICustomMachineryAPI.INSTANCE.logger().error("Error while parsing craft recipe json: {}, skipping...\n{}", recipeId, result.error().get().message());
            throw new JsonParseException("Error while parsing Custom Machine Craft Recipe json: " + recipeId + " error: " + result.error().get().message());
        }
        throw new IllegalStateException("No success nor error when parsing Custom Machine Craft Recipe json: " + recipeId + "This can't happen");
    }

    @Nullable
    @Override
    public CustomCraftRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        ICustomMachineryAPI.INSTANCE.logger().info("Receiving craft recipe: {} from server.", recipeId);
        DataResult<CustomCraftRecipeBuilder> result = CustomCraftRecipeBuilder.CODEC.read(NbtOps.INSTANCE, buffer.readAnySizeNbt());
        if(result.result().isPresent()) {
            ICustomMachineryAPI.INSTANCE.logger().info("Sucessfully received craft recipe: {} from server.", recipeId);
            return result.result().get().build(recipeId);
        } else if(result.error().isPresent()) {
            ICustomMachineryAPI.INSTANCE.logger().error("Error while parsing craft recipe json: {}, skipping...\n{}", recipeId, result.error().get().message());
            throw new IllegalArgumentException("Error while receiving Custom Machine Craft Recipe from server: " + recipeId + " error: " + result.error().get().message());
        }
        throw new IllegalStateException("No success nor error when receiving Custom Machine Craft Recipe: " + recipeId + "from server. This can't happen");
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, CustomCraftRecipe recipe) {
        ICustomMachineryAPI.INSTANCE.logger().info("Sending craft recipe: {} to clients", recipe.getId());
        DataResult<Tag> result = CustomCraftRecipeBuilder.CODEC.encodeStart(NbtOps.INSTANCE, new CustomCraftRecipeBuilder(recipe));
        if(result.result().isPresent()) {
            ICustomMachineryAPI.INSTANCE.logger().info("Sucessfully send craft recipe: {} to clients.", recipe.getId());
            buffer.writeNbt((CompoundTag) result.result().get());
            return;
        } else if(result.error().isPresent()) {
            ICustomMachineryAPI.INSTANCE.logger().error("Error while sending craft recipe: {} to clients.%n{}", recipe.getId(), result.error().get().message());
            throw new IllegalArgumentException("Error while sending Custom Machine Craft Recipe to clients: " + recipe.getId() + " error: " + result.error().get().message());
        }
        throw new IllegalStateException("No success nor error when sending Custom Machine Craft Recipe: " + recipe.getId() + "to clients. This can't happen");
    }
}

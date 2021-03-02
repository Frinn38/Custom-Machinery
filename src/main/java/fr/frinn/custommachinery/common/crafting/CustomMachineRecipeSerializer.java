package fr.frinn.custommachinery.common.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.stream.IntStream;

public class CustomMachineRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<CustomMachineRecipe> {

    @Override
    public CustomMachineRecipe read(ResourceLocation recipeId, JsonObject json) {
        CustomMachineRecipeBuilder builder = new CustomMachineRecipeBuilder();
        builder.withId(recipeId);
        builder.withMachine(new ResourceLocation(JSONUtils.getString(json, "machine")));
        builder.withTime(JSONUtils.getInt(json, "time"));
        JSONUtils.getJsonArray(json, "requirements").forEach(requirementJSON -> {
            ResourceLocation type = new ResourceLocation(JSONUtils.getString(requirementJSON.getAsJsonObject(), "type"));
            if(Registration.REQUIREMENT_TYPE_REGISTRY.containsKey(type))
                builder.withRequirement(Registration.REQUIREMENT_TYPE_REGISTRY.getValue(type).getCodec().decode(JsonOps.INSTANCE, requirementJSON.getAsJsonObject()).resultOrPartial(CustomMachinery.LOGGER::error).orElseThrow(() -> new JsonParseException("Error while parsing requirement: " + type + " in recipe: " + recipeId)).getFirst());
            else throw new JsonParseException("Unknown requirement type: " + type);
        });
        return builder.build();
    }

    @ParametersAreNonnullByDefault
    @Nullable
    @Override
    public CustomMachineRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
        CustomMachineRecipeBuilder builder = new CustomMachineRecipeBuilder();
        builder.withId(recipeId);
        builder.withMachine(new ResourceLocation(buffer.readString()));
        builder.withTime(buffer.readInt());
        IntStream.range(0, buffer.readInt()).forEach((integer) -> {
            try {
                ResourceLocation type = new ResourceLocation(buffer.readString());
                if(Registration.REQUIREMENT_TYPE_REGISTRY.containsKey(type))
                    builder.withRequirement(buffer.func_240628_a_(Registration.REQUIREMENT_TYPE_REGISTRY.getValue(type).getCodec()));
                else throw new IOException("Unknown requirement type: " + type);
            } catch (IOException e) {
                CustomMachinery.LOGGER.warn("Error while receiving recipe: " + recipeId + " from server");
                e.printStackTrace();
            }
        });
        return builder.build();
    }

    @Override
    public void write(PacketBuffer buffer, CustomMachineRecipe recipe) {
        buffer.writeString(recipe.getMachine().toString());
        buffer.writeInt(recipe.getRecipeTime());
        buffer.writeInt(recipe.getRequirements().size());
        recipe.getRequirements().forEach(requirement -> {
            try {
                buffer.writeString(requirement.getType().toString());
                buffer.func_240629_a_(requirement.getType().getCodec(), requirement);
            } catch (IOException e) {
                CustomMachinery.LOGGER.warn("Error while sending recipe: " + recipe.getId() + " to client");
                e.printStackTrace();
            }
        });
    }
}

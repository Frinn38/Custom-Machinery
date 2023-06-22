package fr.frinn.custommachinery.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.MapData;
import com.blamejared.crafttweaker.api.data.visitor.DataToJsonStringVisitor;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.common.crafting.machine.CustomMachineRecipe;
import fr.frinn.custommachinery.common.crafting.machine.CustomMachineRecipeBuilder;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

import java.util.List;

@ZenRegister
@Name(CTConstants.RECIPE_MANAGER_MACHINE)
public class CustomMachineRecipeCTManager implements IRecipeManager<CustomMachineRecipe> {

    public static final CustomMachineRecipeCTManager INSTANCE = new CustomMachineRecipeCTManager();

    @Override
    public RecipeType<CustomMachineRecipe> getRecipeType() {
        return Registration.CUSTOM_MACHINE_RECIPE.get();
    }

    @Override
    public ResourceLocation getBracketResourceLocation() {
        return Registration.CUSTOM_MACHINE_RECIPE.getId();
    }

    @Override
    public void addJsonRecipe(String name, MapData mapData) {
        JsonObject recipeObject = JSON_RECIPE_GSON.fromJson(mapData.accept(DataToJsonStringVisitor.INSTANCE), JsonObject.class);
        DataResult<CustomMachineRecipeBuilder> result = CustomMachineRecipeBuilder.CODEC.read(JsonOps.INSTANCE, recipeObject);
        if(result.error().isPresent() || result.result().isEmpty()) {
            CraftTweakerAPI.LOGGER.error("Couldn't add custom machine recipe {} from json: {}", name, result.error().get().message());
            return;
        }
        ResourceLocation id = ResourceLocation.tryParse(name);
        if(id == null) {
            CraftTweakerAPI.LOGGER.error("Invalid id for custom machine recipe: {}", name);
            return;
        }
        CustomMachineRecipe recipe = result.result().get().build(id);
        CraftTweakerAPI.apply(new ActionAddRecipe<>(this, recipe));
    }

    @Override
    public List<CustomMachineRecipe> getRecipesByOutput(IIngredient output) {
        throw new UnsupportedOperationException("Can't get custom machine recipe by output");
    }

    @Override
    public void remove(IIngredient output) {
        throw new UnsupportedOperationException("Can't remove custom machine recipe by output");
    }

    @Override
    public void removeByInput(IItemStack input) {
        throw new UnsupportedOperationException("Can't remove custom machine recipe by input");
    }

    @Method
    public CustomMachineRecipeCTBuilder create(String machine, int time) {
        return CustomMachineRecipeCTBuilder.create(machine, time);
    }
}

package fr.frinn.custommachinery.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.MapData;
import com.blamejared.crafttweaker.api.data.op.IDataOps;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.mojang.serialization.DataResult;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.crafting.craft.CustomCraftRecipe;
import fr.frinn.custommachinery.common.crafting.craft.CustomCraftRecipeBuilder;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

import java.util.List;

@ZenRegister
@Name(CTConstants.RECIPE_MANAGER_CRAFT)
public class CustomCraftRecipeCTManager implements IRecipeManager<CustomCraftRecipe> {

    public static final CustomCraftRecipeCTManager INSTANCE = new CustomCraftRecipeCTManager();

    @Override
    public RecipeType<CustomCraftRecipe> getRecipeType() {
        return Registration.CUSTOM_CRAFT_RECIPE.get();
    }

    @Override
    public ResourceLocation getBracketResourceLocation() {
        return Registration.CUSTOM_CRAFT_RECIPE.getId();
    }

    @Override
    public void addJsonRecipe(String name, MapData mapData) {
        DataResult<CustomCraftRecipeBuilder> result = CustomCraftRecipeBuilder.CODEC.read(IDataOps.INSTANCE, mapData);
        if(result.error().isPresent() || result.result().isEmpty()) {
            CraftTweakerAPI.getLogger(CustomMachinery.MODID).error("Couldn't add custom craft recipe {} from json: {}", name, result.error().get().message());
            return;
        }
        ResourceLocation id = ResourceLocation.tryParse(name);
        if(id == null) {
            CraftTweakerAPI.getLogger(CustomMachinery.MODID).error("Invalid id for custom craft recipe: {}", name);
            return;
        }
        CustomCraftRecipe recipe = result.result().get().build();
        CraftTweakerAPI.apply(new ActionAddRecipe<>(this, new RecipeHolder<>(id, recipe)));
    }

    @Override
    public List<RecipeHolder<CustomCraftRecipe>> getRecipesByOutput(IIngredient output) {
        throw new UnsupportedOperationException("Can't get custom craft recipe by output");
    }

    @Override
    public void remove(IIngredient output) {
        throw new UnsupportedOperationException("Can't remove custom craft recipe by output");
    }

    @Override
    public void removeByInput(IItemStack input) {
        throw new UnsupportedOperationException("Can't remove custom craft recipe by input");
    }

    @Method
    public CustomCraftRecipeCTBuilder create(String machine, IItemStack output) {
        return CustomCraftRecipeCTBuilder.create(machine, output);
    }
}

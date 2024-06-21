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
import fr.frinn.custommachinery.common.crafting.machine.CustomMachineRecipe;
import fr.frinn.custommachinery.common.crafting.machine.CustomMachineRecipeBuilder;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
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
        DataResult<CustomMachineRecipeBuilder> result = CustomMachineRecipeBuilder.CODEC.read(IDataOps.INSTANCE, mapData);
        if(result.error().isPresent() || result.result().isEmpty()) {
            CraftTweakerAPI.getLogger(CustomMachinery.MODID).error("Couldn't add custom machine recipe {} from json: {}", name, result.error().get().message());
            return;
        }
        ResourceLocation id = ResourceLocation.tryParse(name);
        if(id == null) {
            CraftTweakerAPI.getLogger(CustomMachinery.MODID).error("Invalid id for custom machine recipe: {}", name);
            return;
        }
        CustomMachineRecipe recipe = result.result().get().build();
        CraftTweakerAPI.apply(new ActionAddRecipe<>(this, new RecipeHolder<>(id, recipe)));
    }

    @Override
    public List<RecipeHolder<CustomMachineRecipe>> getRecipesByOutput(IIngredient output) {
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

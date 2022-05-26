package fr.frinn.custommachinery.common.integration.jei;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.crafting.CustomMachineRecipe;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class MachineRecipeTypes {

    private final Map<ResourceLocation, RecipeType<CustomMachineRecipe>> TYPES = new HashMap<>();

    public MachineRecipeTypes() {
        for(ResourceLocation id : CustomMachinery.MACHINES.keySet())
            TYPES.put(id, RecipeType.create(id.getNamespace(), id.getPath(), CustomMachineRecipe.class));
    }

    @Nullable
    public RecipeType<CustomMachineRecipe> fromID(ResourceLocation id) {
        return TYPES.get(id);
    }
}

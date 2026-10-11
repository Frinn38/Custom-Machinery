package fr.frinn.custommachinery.client.integration.jei;

import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.common.crafting.craft.CustomCraftRecipe;
import fr.frinn.custommachinery.common.crafting.machine.CustomMachineRecipe;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CMRecipeTypes {

    private static final Map<Identifier, IRecipeType<RecipeHolder<CustomMachineRecipe>>> MACHINE_TYPES = new HashMap<>();
    private static final Map<Identifier, IRecipeType<RecipeHolder<CustomCraftRecipe>>> CRAFT_TYPES = new HashMap<>();

    @Nullable
    public static IRecipeType<RecipeHolder<CustomMachineRecipe>> machine(Identifier id) {
        return MACHINE_TYPES.get(id);
    }

    @Nullable
    public static IRecipeType<RecipeHolder<CustomCraftRecipe>> craft(Identifier id) {
        return CRAFT_TYPES.get(id);
    }

    @SuppressWarnings("unchecked")
    public static IRecipeType<RecipeHolder<CustomMachineRecipe>> createMachine(Identifier id) {
        IRecipeType<RecipeHolder<CustomMachineRecipe>> type = IRecipeType.create(id, (Class<? extends RecipeHolder<CustomMachineRecipe>>) (Object) RecipeHolder.class);
        MACHINE_TYPES.put(id, type);
        return type;
    }

    @SuppressWarnings("unchecked")
    public static IRecipeType<RecipeHolder<CustomCraftRecipe>> createCraft(Identifier id) {
        IRecipeType<RecipeHolder<CustomCraftRecipe>> type = IRecipeType.create(id,  (Class<? extends RecipeHolder<CustomCraftRecipe>>) (Object) RecipeHolder.class);
        CRAFT_TYPES.put(id, type);
        return type;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static List<IRecipeType<RecipeHolder<IMachineRecipe>>> all() {
        List<IRecipeType<RecipeHolder<IMachineRecipe>>> list = new ArrayList<>();
        list.addAll((Collection)MACHINE_TYPES.values());
        list.addAll((Collection)CRAFT_TYPES.values());
        return list;
    }
}

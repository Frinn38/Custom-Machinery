package fr.frinn.custommachinery.client.integration.jei;

import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.common.crafting.craft.CustomCraftRecipe;
import fr.frinn.custommachinery.common.crafting.machine.CustomMachineRecipe;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CMRecipeTypes {

    private static final Map<ResourceLocation, RecipeType<RecipeHolder<CustomMachineRecipe>>> MACHINE_TYPES = new HashMap<>();
    private static final Map<ResourceLocation, RecipeType<RecipeHolder<CustomCraftRecipe>>> CRAFT_TYPES = new HashMap<>();

    @Nullable
    public static RecipeType<RecipeHolder<CustomMachineRecipe>> machine(ResourceLocation id) {
        return MACHINE_TYPES.get(id);
    }

    @Nullable
    public static RecipeType<RecipeHolder<CustomCraftRecipe>> craft(ResourceLocation id) {
        return CRAFT_TYPES.get(id);
    }

    public static RecipeType<RecipeHolder<CustomMachineRecipe>> createMachine(ResourceLocation id) {
        RecipeType<RecipeHolder<CustomMachineRecipe>> type = RecipeType.createRecipeHolderType(id);
        MACHINE_TYPES.put(id, type);
        return type;
    }

    public static RecipeType<RecipeHolder<CustomCraftRecipe>> createCraft(ResourceLocation id) {
        RecipeType<RecipeHolder<CustomCraftRecipe>> type = RecipeType.createRecipeHolderType(id);
        CRAFT_TYPES.put(id, type);
        return type;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static List<RecipeType<RecipeHolder<IMachineRecipe>>> all() {
        List<RecipeType<RecipeHolder<IMachineRecipe>>> list = new ArrayList<>();
        list.addAll((Collection)MACHINE_TYPES.values());
        list.addAll((Collection)CRAFT_TYPES.values());
        return list;
    }
}

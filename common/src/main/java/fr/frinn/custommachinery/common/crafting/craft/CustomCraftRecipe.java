package fr.frinn.custommachinery.common.crafting.craft;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

public class CustomCraftRecipe implements Recipe<Container>, IMachineRecipe {

    public static final List<RequirementType<?>> FORBIDDEN_REQUIREMENTS = Lists.newArrayList(
            Registration.ENERGY_PER_TICK_REQUIREMENT.get(),
            Registration.FLUID_PER_TICK_REQUIREMENT.get()
    );

    private final ResourceLocation id;
    private final ResourceLocation machine;
    private final ItemStack output;
    private final List<IRequirement<?>> requirements;
    private final List<IRequirement<?>> jeiRequirements;
    private final int priority;
    private final int jeiPriority;

    public CustomCraftRecipe(ResourceLocation id, ResourceLocation machine, ItemStack output, List<IRequirement<?>> requirements, List<IRequirement<?>> jeiRequirements, int priority, int jeiPriority) {
        this.id = id;
        this.machine = machine;
        this.output = output;
        this.requirements = validateRequirements(requirements);
        this.jeiRequirements = validateRequirements(jeiRequirements);
        this.priority = priority;
        this.jeiPriority = jeiPriority;
    }

    public ResourceLocation getMachine() {
        return this.machine;
    }

    public ItemStack getOutput() {
        return this.output;
    }

    private List<IRequirement<?>> validateRequirements(List<IRequirement<?>> requirements) {
        return requirements.stream().filter(requirement -> {
            if(!FORBIDDEN_REQUIREMENTS.contains(requirement.getType()) && requirement.getMode() == RequirementIOMode.INPUT)
                return true;
            ICustomMachineryAPI.INSTANCE.logger().error("Invalid requirement: {}", IRequirement.CODEC.encodeStart(JsonOps.INSTANCE, requirement).result().map(JsonElement::toString).orElse(requirement.getType().toString()));
            return false;
        }).toList();
    }

    @Override
    public ResourceLocation getRecipeId() {
        return this.id;
    }

    @Override
    public int getRecipeTime() {
        return 0;
    }

    @Override
    public List<IRequirement<?>> getRequirements() {
        return this.requirements;
    }

    @Override
    public List<IRequirement<?>> getJeiRequirements() {
        return this.jeiRequirements;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public int getJeiPriority() {
        return this.jeiPriority;
    }

    @Override
    public boolean shouldResetOnError() {
        return false;
    }

    /** VANILLA RECIPE STUFF **/

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container) {
        return null;
    }

    @Override
    public boolean canCraftInDimensions(int i, int j) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        return this.output;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registration.CUSTOM_CRAFT_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return Registration.CUSTOM_CRAFT_RECIPE.get();
    }
}

package fr.frinn.custommachinery.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class CustomMachineRecipeBuilder {

    public static final Codec<CustomMachineRecipeBuilder> CODEC = RecordCodecBuilder.create(recipeBuilderInstance -> recipeBuilderInstance.group(
            ResourceLocation.CODEC.fieldOf("machine").forGetter(builder -> builder.machine),
            Codec.INT.fieldOf("time").forGetter(builder -> builder.time),
            IRequirement.CODEC.listOf().optionalFieldOf("requirements", new ArrayList<>()).forGetter(builder -> builder.requirements),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(builder -> builder.priority)
    ).apply(recipeBuilderInstance, (machine, time, requirements, priority) -> {
        CustomMachineRecipeBuilder builder = new CustomMachineRecipeBuilder(machine, time);
        requirements.forEach(builder::withRequirement);
        builder.withPriority(priority);
        return builder;
    }));

    private ResourceLocation machine;
    private int time;
    private List<IRequirement<?>> requirements = new ArrayList<>();
    private int priority = 0;

    public CustomMachineRecipeBuilder(ResourceLocation machine, int time) {
        this.machine = machine;
        this.time = time;
    }

    public CustomMachineRecipeBuilder(CustomMachineRecipe recipe) {
        this.machine = recipe.getMachine();
        this.time = recipe.getRecipeTime();
        this.requirements = recipe.getRequirements();
        this.priority = recipe.getPriority();
    }

    public CustomMachineRecipeBuilder withRequirement(IRequirement<?> requirement) {
        this.requirements.add(requirement);
        return this;
    }

    public CustomMachineRecipeBuilder withPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public CustomMachineRecipe build(ResourceLocation id) {
        return new CustomMachineRecipe(id, this.machine, this.time, this.requirements, this.priority);
    }
}

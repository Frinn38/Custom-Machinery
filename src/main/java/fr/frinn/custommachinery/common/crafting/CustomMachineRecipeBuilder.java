package fr.frinn.custommachinery.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.utils.CodecLogger;
import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.util.Codecs;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CustomMachineRecipeBuilder {

    public static final Codec<CustomMachineRecipeBuilder> CODEC = RecordCodecBuilder.create(recipeBuilderInstance -> recipeBuilderInstance.group(
            ResourceLocation.CODEC.fieldOf("machine").forGetter(builder -> builder.machine),
            Codec.INT.fieldOf("time").forGetter(builder -> builder.time),
            CodecLogger.loggedOptional(Codecs.list(IRequirement.CODEC),"requirements", Collections.emptyList()).forGetter(builder -> builder.requirements),
            CodecLogger.loggedOptional(Codecs.list(IRequirement.CODEC),"jei", Collections.emptyList()).forGetter(builder -> builder.jeiRequirements),
            CodecLogger.loggedOptional(Codec.INT,"priority", 0).forGetter(builder -> builder.priority)
    ).apply(recipeBuilderInstance, (machine, time, requirements, jeiRequirements, priority) -> {
        CustomMachineRecipeBuilder builder = new CustomMachineRecipeBuilder(machine, time);
        requirements.forEach(builder::withRequirement);
        jeiRequirements.forEach(builder::withJeiRequirement);
        builder.withPriority(priority);
        return builder;
    }));

    private ResourceLocation machine;
    private int time;
    private List<IRequirement<?>> requirements = new ArrayList<>();
    private List<IRequirement<?>> jeiRequirements = new ArrayList<>();
    private int priority = 0;

    public CustomMachineRecipeBuilder(ResourceLocation machine, int time) {
        this.machine = machine;
        this.time = time;
    }

    public CustomMachineRecipeBuilder(CustomMachineRecipe recipe) {
        this.machine = recipe.getMachine();
        this.time = recipe.getRecipeTime();
        this.requirements = recipe.getRequirements();
        this.jeiRequirements = recipe.getJeiRequirements();
        this.priority = recipe.getPriority();
    }

    public CustomMachineRecipeBuilder withRequirement(IRequirement<?> requirement) {
        this.requirements.add(requirement);
        return this;
    }

    public CustomMachineRecipeBuilder withJeiRequirement(IRequirement<?> requirement) {
        this.jeiRequirements.add(requirement);
        return this;
    }

    public CustomMachineRecipeBuilder withPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public CustomMachineRecipe build(ResourceLocation id) {
        return new CustomMachineRecipe(id, this.machine, this.time, this.requirements, this.jeiRequirements, this.priority);
    }

    @Override
    public String toString() {
        return "CustomMachineRecipe{" +
                "machine=" + machine +
                ", time=" + time +
                ", requirements=" + requirements.stream().map(requirement -> requirement.getType().getRegistryName()).collect(Collectors.toList()) +
                ", priority=" + priority +
                '}';
    }
}

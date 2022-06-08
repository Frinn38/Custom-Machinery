package fr.frinn.custommachinery.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.apiimpl.codec.CodecLogger;
import fr.frinn.custommachinery.common.util.Codecs;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomMachineRecipeBuilder {

    public static final Codec<CustomMachineRecipeBuilder> CODEC = RecordCodecBuilder.create(recipeBuilderInstance -> recipeBuilderInstance.group(
            ResourceLocation.CODEC.fieldOf("machine").forGetter(builder -> builder.machine),
            Codec.INT.fieldOf("time").forGetter(builder -> builder.time),
            CodecLogger.loggedOptional(Codecs.list(IRequirement.CODEC),"requirements", Collections.emptyList()).forGetter(builder -> builder.requirements),
            CodecLogger.loggedOptional(Codecs.list(IRequirement.CODEC),"jei", Collections.emptyList()).forGetter(builder -> builder.jeiRequirements),
            CodecLogger.loggedOptional(Codec.INT,"priority", 0).forGetter(builder -> builder.priority),
            CodecLogger.loggedOptional(Codec.INT,"jeiPriority", 0).forGetter(builder -> builder.jeiPriority)
    ).apply(recipeBuilderInstance, (machine, time, requirements, jeiRequirements, priority, jeiPriority) -> {
        CustomMachineRecipeBuilder builder = new CustomMachineRecipeBuilder(machine, time);
        requirements.forEach(builder::withRequirement);
        jeiRequirements.forEach(builder::withJeiRequirement);
        builder.withPriority(priority);
        builder.withJeiPriority(jeiPriority);
        return builder;
    }));

    private final ResourceLocation machine;
    private final int time;
    private List<IRequirement<?>> requirements = new ArrayList<>();
    private List<IRequirement<?>> jeiRequirements = new ArrayList<>();
    private int priority = 0;
    private int jeiPriority = 0;

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

    public CustomMachineRecipeBuilder withJeiPriority(int jeiPriority) {
        this.jeiPriority = jeiPriority;
        return this;
    }

    public CustomMachineRecipe build(ResourceLocation id) {
        return new CustomMachineRecipe(id, this.machine, this.time, this.requirements, this.jeiRequirements, this.priority, this.jeiPriority);
    }

    @Override
    public String toString() {
        return "CustomMachineRecipe{" +
                "machine=" + machine +
                ", time=" + time +
                ", requirements=" + requirements.stream().map(requirement -> requirement.getType().getRegistryName()).toList() +
                ", priority=" + priority +
                ", jeiPriority=" + jeiPriority +
                '}';
    }
}

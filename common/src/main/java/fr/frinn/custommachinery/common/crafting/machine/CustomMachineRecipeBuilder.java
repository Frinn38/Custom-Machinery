package fr.frinn.custommachinery.common.crafting.machine;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.crafting.AbstractRecipeBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;

public class CustomMachineRecipeBuilder extends AbstractRecipeBuilder<CustomMachineRecipe> {

    public static final NamedCodec<CustomMachineRecipeBuilder> CODEC = NamedCodec.record(recipeBuilderInstance ->
            recipeBuilderInstance.group(
                    DefaultCodecs.RESOURCE_LOCATION.fieldOf("machine").forGetter(AbstractRecipeBuilder::getMachine),
                    NamedCodec.INT.fieldOf("time").forGetter(builder -> builder.time),
                    IRequirement.CODEC.listOf().optionalFieldOf("requirements", Collections.emptyList()).forGetter(AbstractRecipeBuilder::getRequirements),
                    IRequirement.CODEC.listOf().optionalFieldOf("jei", Collections.emptyList()).forGetter(AbstractRecipeBuilder::getJeiRequirements),
                    NamedCodec.INT.optionalFieldOf("priority", 0).forGetter(AbstractRecipeBuilder::getPriority),
                    NamedCodec.INT.optionalFieldOf("jeiPriority", 0).forGetter(AbstractRecipeBuilder::getJeiPriority),
                    NamedCodec.BOOL.optionalFieldOf("error", true).forGetter(builder -> !builder.resetOnError)
            ).apply(recipeBuilderInstance, (machine, time, requirements, jeiRequirements, priority, jeiPriority, error) -> {
                    CustomMachineRecipeBuilder builder = new CustomMachineRecipeBuilder(machine, time);
                    requirements.forEach(builder::withRequirement);
                    jeiRequirements.forEach(builder::withJeiRequirement);
                    builder.withPriority(priority);
                    builder.withJeiPriority(jeiPriority);
                    if (!error)
                        builder.setResetOnError();
                    return builder;
            }), "Machine recipe builder"
    );

    private final int time;
    private boolean resetOnError = false;

    public CustomMachineRecipeBuilder(ResourceLocation machine, int time) {
        super(machine);
        this.time = time;
    }

    public CustomMachineRecipeBuilder(CustomMachineRecipe recipe) {
        super(recipe);
        this.time = recipe.getRecipeTime();
        this.resetOnError = recipe.shouldResetOnError();
    }

    public CustomMachineRecipeBuilder setResetOnError() {
        this.resetOnError = true;
        return this;
    }

    public CustomMachineRecipe build(ResourceLocation id) {
        return new CustomMachineRecipe(id, this.getMachine(), this.time, this.getRequirements(), this.getJeiRequirements(), this.getPriority(), this.getJeiPriority(), this.resetOnError);
    }

    @Override
    public String toString() {
        return "CustomMachineRecipe{" +
                "machine=" + getMachine() +
                ", time=" + time +
                ", requirements=" + getRequirements().stream().map(requirement -> requirement.getType().getId()).toList() +
                ", jeiRequirements=" + getJeiRequirements().stream().map(requirement -> requirement.getType().getId()).toList() +
                ", priority=" + getPriority() +
                ", jeiPriority=" + getJeiPriority() +
                ", resetOnError=" + resetOnError +
                '}';
    }
}

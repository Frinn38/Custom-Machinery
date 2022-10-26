package fr.frinn.custommachinery.common.crafting.machine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.impl.codec.CodecLogger;
import fr.frinn.custommachinery.impl.crafting.AbstractRecipeBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;

public class CustomMachineRecipeBuilder extends AbstractRecipeBuilder<CustomMachineRecipe> {

    public static final Codec<CustomMachineRecipeBuilder> CODEC = RecordCodecBuilder.create(recipeBuilderInstance -> recipeBuilderInstance.group(
            ResourceLocation.CODEC.fieldOf("machine").forGetter(AbstractRecipeBuilder::getMachine),
            Codec.INT.fieldOf("time").forGetter(builder -> builder.time),
            CodecLogger.loggedOptional(Codecs.list(IRequirement.CODEC),"requirements", Collections.emptyList()).forGetter(AbstractRecipeBuilder::getRequirements),
            CodecLogger.loggedOptional(Codecs.list(IRequirement.CODEC),"jei", Collections.emptyList()).forGetter(AbstractRecipeBuilder::getJeiRequirements),
            CodecLogger.loggedOptional(Codec.INT,"priority", 0).forGetter(AbstractRecipeBuilder::getPriority),
            CodecLogger.loggedOptional(Codec.INT,"jeiPriority", 0).forGetter(AbstractRecipeBuilder::getJeiPriority),
            CodecLogger.loggedOptional(Codec.BOOL, "error", true).forGetter(builder -> !builder.resetOnError)
    ).apply(recipeBuilderInstance, (machine, time, requirements, jeiRequirements, priority, jeiPriority, error) -> {
        CustomMachineRecipeBuilder builder = new CustomMachineRecipeBuilder(machine, time);
        requirements.forEach(builder::withRequirement);
        jeiRequirements.forEach(builder::withJeiRequirement);
        builder.withPriority(priority);
        builder.withJeiPriority(jeiPriority);
        if(!error)
            builder.setResetOnError();
        return builder;
    }));

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

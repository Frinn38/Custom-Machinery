package fr.frinn.custommachinery.common.crafting.craft;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.impl.codec.CodecLogger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomCraftRecipeBuilder {

    public static final Codec<CustomCraftRecipeBuilder> CODEC = RecordCodecBuilder.create(builderInstance ->
            builderInstance.group(
                    ResourceLocation.CODEC.fieldOf("machine").forGetter(builder -> builder.machine),
                    ItemStack.CODEC.fieldOf("output").forGetter(builder -> builder.output),
                    CodecLogger.loggedOptional(Codecs.list(IRequirement.CODEC),"requirements", Collections.emptyList()).forGetter(builder -> builder.requirements),
                    CodecLogger.loggedOptional(Codecs.list(IRequirement.CODEC),"jei", Collections.emptyList()).forGetter(builder -> builder.jeiRequirements),
                    CodecLogger.loggedOptional(Codec.INT,"priority", 0).forGetter(builder -> builder.priority),
                    CodecLogger.loggedOptional(Codec.INT,"jeiPriority", 0).forGetter(builder -> builder.jeiPriority)
            ).apply(builderInstance, (machine, output, requirements, jeiRequirements, priority, jeiPriority) -> {
                CustomCraftRecipeBuilder builder = new CustomCraftRecipeBuilder(machine, output);
                requirements.forEach(builder::withRequirement);
                jeiRequirements.forEach(builder::withJeiRequirement);
                builder.withPriority(priority);
                builder.withJeiPriority(jeiPriority);
                return builder;
            })
    );

    private final ResourceLocation machine;
    private final ItemStack output;
    private List<IRequirement<?>> requirements = new ArrayList<>();
    private List<IRequirement<?>> jeiRequirements = new ArrayList<>();
    private int priority = 0;
    private int jeiPriority = 0;

    public CustomCraftRecipeBuilder(ResourceLocation machine, ItemStack output) {
        this.machine = machine;
        this.output = output;
    }

    public CustomCraftRecipeBuilder(CustomCraftRecipe recipe) {
        this.machine = recipe.getMachine();
        this.output = recipe.getOutput();
        this.requirements = recipe.getRequirements();
        this.jeiRequirements = recipe.getJeiRequirements();
        this.priority = recipe.getPriority();
        this.jeiPriority = recipe.getJeiPriority();
    }

    public CustomCraftRecipeBuilder withRequirement(IRequirement<?> requirement) {
        this.requirements.add(requirement);
        return this;
    }

    public CustomCraftRecipeBuilder withJeiRequirement(IRequirement<?> requirement) {
        this.jeiRequirements.add(requirement);
        return this;
    }

    public CustomCraftRecipeBuilder withPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public CustomCraftRecipeBuilder withJeiPriority(int jeiPriority) {
        this.jeiPriority = jeiPriority;
        return this;
    }

    public CustomCraftRecipe build(ResourceLocation id) {
        return new CustomCraftRecipe(id, this.machine, this.output, this.requirements, this.jeiRequirements, this.priority, this.jeiPriority);
    }
}

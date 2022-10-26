package fr.frinn.custommachinery.common.crafting.craft;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.impl.codec.CodecLogger;
import fr.frinn.custommachinery.impl.crafting.AbstractRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;

public class CustomCraftRecipeBuilder extends AbstractRecipeBuilder<CustomCraftRecipe> {

    public static final Codec<CustomCraftRecipeBuilder> CODEC = RecordCodecBuilder.create(builderInstance ->
            builderInstance.group(
                    ResourceLocation.CODEC.fieldOf("machine").forGetter(AbstractRecipeBuilder::getMachine),
                    ItemStack.CODEC.fieldOf("output").forGetter(builder -> builder.output),
                    CodecLogger.loggedOptional(Codecs.list(IRequirement.CODEC),"requirements", Collections.emptyList()).forGetter(AbstractRecipeBuilder::getRequirements),
                    CodecLogger.loggedOptional(Codecs.list(IRequirement.CODEC),"jei", Collections.emptyList()).forGetter(AbstractRecipeBuilder::getJeiRequirements),
                    CodecLogger.loggedOptional(Codec.INT,"priority", 0).forGetter(AbstractRecipeBuilder::getPriority),
                    CodecLogger.loggedOptional(Codec.INT,"jeiPriority", 0).forGetter(AbstractRecipeBuilder::getJeiPriority)
            ).apply(builderInstance, (machine, output, requirements, jeiRequirements, priority, jeiPriority) -> {
                CustomCraftRecipeBuilder builder = new CustomCraftRecipeBuilder(machine, output);
                requirements.forEach(builder::withRequirement);
                jeiRequirements.forEach(builder::withJeiRequirement);
                builder.withPriority(priority);
                builder.withJeiPriority(jeiPriority);
                return builder;
            })
    );

    private final ItemStack output;

    public CustomCraftRecipeBuilder(ResourceLocation machine, ItemStack output) {
        super(machine);
        this.output = output;
    }

    public CustomCraftRecipeBuilder(CustomCraftRecipe recipe) {
        super(recipe);
        this.output = recipe.getOutput();
    }

    @Override
    public CustomCraftRecipe build(ResourceLocation id) {
        return new CustomCraftRecipe(id, this.getMachine(), this.output, this.getRequirements(), this.getJeiRequirements(), this.getPriority(), this.getJeiPriority());
    }
}

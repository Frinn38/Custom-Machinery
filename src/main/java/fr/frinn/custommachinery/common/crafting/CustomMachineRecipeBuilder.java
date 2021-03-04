package fr.frinn.custommachinery.common.crafting;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomMachineRecipeBuilder {

    private ResourceLocation id;
    private ResourceLocation machine;
    private int time;
    private List<IRequirement<?>> inputRequirements = new ArrayList<>();
    private List<IRequirement<?>> outputRequirements = new ArrayList<>();

    public CustomMachineRecipeBuilder withId(ResourceLocation id) {
        this.id = id;
        return this;
    }

    public CustomMachineRecipeBuilder withMachine(ResourceLocation machine) {
        this.machine = machine;
        return this;
    }

    public CustomMachineRecipeBuilder withTime(int time) {
        this.time = time;
        return this;
    }

    public CustomMachineRecipeBuilder withRequirement(IRequirement<?> requirement) {
        if(requirement.getMode() == IRequirement.MODE.INPUT)
            this.inputRequirements.add(requirement);
        else
            this.outputRequirements.add(requirement);
        return this;
    }

    public CustomMachineRecipe build() {
        List<IRequirement<?>> requirements = Lists.newArrayList(this.inputRequirements);
        requirements.addAll(this.outputRequirements);
        return new CustomMachineRecipe(this.id, this.machine, this.time, requirements);
    }
}

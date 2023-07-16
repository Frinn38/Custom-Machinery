package fr.frinn.custommachinery.common.integration.kubejs;

import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.script.ScriptType;
import fr.frinn.custommachinery.api.crafting.IRecipeBuilder;
import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.IChanceableRequirement;
import fr.frinn.custommachinery.api.requirement.IDelayedRequirement;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.slf4j.helpers.MessageFormatter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractRecipeJSBuilder<T extends IRecipeBuilder<? extends Recipe<?>>> extends RecipeJS implements RecipeJSBuilder {

    public static final Map<ResourceLocation, Map<ResourceLocation, Integer>> IDS = new HashMap<>();

    private final ResourceLocation typeID;
    private ResourceLocation machine;
    private IRequirement<?> lastRequirement;
    private boolean jei = false;

    public AbstractRecipeJSBuilder(ResourceLocation typeID) {
        this.typeID = typeID;
    }

    public abstract T makeBuilder(ResourceLocation machine);

    @Override
    public void afterLoaded() {
        super.afterLoaded();
        this.machine = ResourceLocation.tryParse(getValue(CustomMachineryRecipeSchemas.MACHINE_ID));
        if(this.machine == null)
            throw new RecipeExceptionJS("Invalid machine id: " + getValue(CustomMachineryRecipeSchemas.MACHINE_ID));

        int uniqueID = IDS.computeIfAbsent(this.typeID, id -> new HashMap<>()).computeIfAbsent(this.machine, m -> 0);
        IDS.get(this.typeID).put(this.machine, uniqueID + 1);
        this.id = new ResourceLocation("kubejs", this.typeID.getPath() + "/" + this.machine.getNamespace() + "/" + this.machine.getPath() + "/" + uniqueID);
    }

    @Override
    public Recipe<?> createRecipe() {
        if(this.changed || this.newRecipe)
            serialize();

        final T builder = makeBuilder(this.machine);

        Arrays.stream(getValue(CustomMachineryRecipeSchemas.REQUIREMENTS)).forEach(builder::withRequirement);
        Arrays.stream(getValue(CustomMachineryRecipeSchemas.JEI_REQUIREMENTS)).forEach(builder::withJeiRequirement);

        builder.withPriority(getValue(CustomMachineryRecipeSchemas.PRIORITY));
        builder.withJeiPriority(getValue(CustomMachineryRecipeSchemas.JEI_PRIORITY));

        if(getValue(CustomMachineryRecipeSchemas.HIDDEN))
            builder.hide();

        return builder.build(getOrCreateId());
    }

    @Override
    public String getFromToString() {
        return Objects.requireNonNull(createRecipe()).toString();
    }

    public AbstractRecipeJSBuilder<T> jei() {
        this.jei = true;
        return this;
    }

    public AbstractRecipeJSBuilder<T> priority(int priority) {
        if(!this.jei)
            setValue(CustomMachineryRecipeSchemas.PRIORITY, priority);
        else
            setValue(CustomMachineryRecipeSchemas.JEI_PRIORITY, priority);
        return this;
    }

    public AbstractRecipeJSBuilder<T> chance(double chance) {
        if(this.lastRequirement != null && this.lastRequirement instanceof IChanceableRequirement)
            ((IChanceableRequirement<?>)this.lastRequirement).setChance(chance);
        else
            ScriptType.SERVER.console.warn("Can't set chance for requirement: " + this.lastRequirement);
        return this;
    }

    public AbstractRecipeJSBuilder<T> hide() {
        setValue(CustomMachineryRecipeSchemas.HIDDEN, true);
        return this;
    }

    public AbstractRecipeJSBuilder<T> delay(double delay) {
        if (this.lastRequirement != null && this.lastRequirement instanceof IDelayedRequirement<?>)
            ((IDelayedRequirement<?>) this.lastRequirement).setDelay(delay);
        else
            ScriptType.SERVER.console.warn("Can't set delay for requirement: " + this.lastRequirement);
        return this;
    }

    @Override
    public AbstractRecipeJSBuilder<T> addRequirement(IRequirement<?> requirement) {
        this.lastRequirement = requirement;
        if(!this.jei)
            setValue(CustomMachineryRecipeSchemas.REQUIREMENTS, Utils.addToArray(getValue(CustomMachineryRecipeSchemas.REQUIREMENTS), requirement));
        else
            setValue(CustomMachineryRecipeSchemas.JEI_REQUIREMENTS, Utils.addToArray(getValue(CustomMachineryRecipeSchemas.JEI_REQUIREMENTS), requirement));
        return this;
    }

    @Override
    public RecipeJSBuilder error(String error, Object... args) {
        throw new RecipeExceptionJS(MessageFormatter.arrayFormat(error, args).getMessage());
    }
}

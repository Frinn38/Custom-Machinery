package fr.frinn.custommachinery.common.integration.kubejs;

import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.script.ScriptType;
import fr.frinn.custommachinery.api.crafting.IRecipeBuilder;
import fr.frinn.custommachinery.api.integration.jei.DisplayInfoTemplate;
import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RecipeRequirement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.helpers.MessageFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class AbstractRecipeJSBuilder<T extends IRecipeBuilder<? extends Recipe<?>>> extends KubeRecipe implements RecipeJSBuilder {

    public static final Map<ResourceLocation, Map<ResourceLocation, Integer>> IDS = new HashMap<>();

    private final ResourceLocation typeID;
    private RecipeRequirement<?, ?> lastRequirement;
    private boolean jei = false;

    public AbstractRecipeJSBuilder(ResourceLocation typeID) {
        this.typeID = typeID;
    }

    @Override
    public void afterLoaded() {
        super.afterLoaded();
        ResourceLocation machine = getValue(CustomMachineryRecipeSchemas.MACHINE_ID);
        if(machine == null)
            throw new KubeRuntimeException("Invalid machine id: " + getValue(CustomMachineryRecipeSchemas.MACHINE_ID));

        if(this.newRecipe) {
            int uniqueID = IDS.computeIfAbsent(this.typeID, id -> new HashMap<>()).computeIfAbsent(machine, m -> 0);
            IDS.get(this.typeID).put(machine, uniqueID + 1);
            this.id = ResourceLocation.fromNamespaceAndPath("kubejs", this.typeID.getPath() + "/" + machine.getNamespace() + "/" + machine.getPath() + "/" + uniqueID);
        }
    }

    @Override
    public @Nullable RecipeHolder<?> createRecipe() {
        if(this.removed)
            return null;

        if(!this.newRecipe)
            return super.createRecipe();

        T builder = makeBuilder();

        for (RecipeRequirement<?, ?> requirement : getValue(CustomMachineryRecipeSchemas.REQUIREMENTS))
            builder.withRequirement(requirement);
        for (RecipeRequirement<?, ?> requirement : getValue(CustomMachineryRecipeSchemas.JEI_REQUIREMENTS))
            builder.withJeiRequirement(requirement);

        builder.withPriority(getValue(CustomMachineryRecipeSchemas.PRIORITY));
        builder.withJeiPriority(getValue(CustomMachineryRecipeSchemas.JEI_PRIORITY));

        if(getValue(CustomMachineryRecipeSchemas.HIDDEN))
            builder.hide();

        ResourceLocation id = getOrCreateId();
        return new RecipeHolder<>(id, builder.build());
    }

    @Override
    public String getFromToString() {
        return Objects.requireNonNull(createRecipe()).toString();
    }

    public abstract T makeBuilder();

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
        if(this.lastRequirement != null)
            this.lastRequirement.setChance(chance);
        else
            ScriptType.SERVER.console.warn("Can't set chance before adding requirements");
        return this;
    }

    public AbstractRecipeJSBuilder<T> info(Consumer<DisplayInfoTemplate> consumer) {
        if(this.lastRequirement == null)
            this.error("Can't add info on a null requirement !");
        try {
            DisplayInfoTemplate template = new DisplayInfoTemplate();
            consumer.accept(template);
            this.lastRequirement.info = template;
        } catch (Exception e) {
            this.error("Error when adding custom display info on requirement {}\n{}", this.lastRequirement, e);
        }
        return this;
    }

    public AbstractRecipeJSBuilder<T> hide() {
        setValue(CustomMachineryRecipeSchemas.HIDDEN, true);
        return this;
    }

    public AbstractRecipeJSBuilder<T> delay(double delay) {
        if(this.lastRequirement != null)
            this.lastRequirement.setDelay(delay);
        else
            ScriptType.SERVER.console.warn("Can't set delay for requirement: " + this.lastRequirement);
        return this;
    }

    @Override
    public AbstractRecipeJSBuilder<T> addRequirement(IRequirement<?> requirement) {
        this.lastRequirement = new RecipeRequirement<>(requirement);
        if(!this.jei)
            setValue(CustomMachineryRecipeSchemas.REQUIREMENTS, addToList(CustomMachineryRecipeSchemas.REQUIREMENTS, this.lastRequirement));
        else
            setValue(CustomMachineryRecipeSchemas.JEI_REQUIREMENTS, addToList(CustomMachineryRecipeSchemas.JEI_REQUIREMENTS, this.lastRequirement));
        return this;
    }

    @Override
    public RecipeJSBuilder error(String error, Object... args) {
        throw new KubeRuntimeException(MessageFormatter.arrayFormat(error, args).getMessage());
    }

    protected <E> List<E> addToList(RecipeKey<List<E>> key, E element) {
        List<E> list = new ArrayList<>(getValue(key));
        list.add(element);
        return list;
    }
}

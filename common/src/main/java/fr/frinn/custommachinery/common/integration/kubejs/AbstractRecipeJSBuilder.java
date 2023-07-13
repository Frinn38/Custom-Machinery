package fr.frinn.custommachinery.common.integration.kubejs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.script.ScriptType;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.crafting.IRecipeBuilder;
import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.IChanceableRequirement;
import fr.frinn.custommachinery.api.requirement.IDelayedRequirement;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractRecipeJSBuilder<T extends IRecipeBuilder<? extends Recipe<?>>> extends RecipeJS implements RecipeJSBuilder {

    public static final Map<ResourceLocation, Map<ResourceLocation, Integer>> IDS = new HashMap<>();

    private final ResourceLocation id;
    private final NamedCodec<T> builderCodec;
    private T builder;
    private ResourceLocation machine;
    private IRequirement<?> lastRequirement;
    private boolean jei = false;

    public AbstractRecipeJSBuilder(ResourceLocation id, NamedCodec<T> builderCodec) {
        this.id = id;
        this.builderCodec = builderCodec;
    }

    public abstract T makeBuilder(ResourceLocation machine);

    public T builder() {
        return this.builder;
    }

    @Override
    public void afterLoaded() {
        super.afterLoaded();
        ResourceLocation machine = ResourceLocation.tryParse(getValue(CustomMachineryRecipeSchemas.MACHINE_ID));
        if(machine == null)
            throw new RecipeExceptionJS("Invalid machine id: " + getValue(CustomMachineryRecipeSchemas.MACHINE_ID));
        this.builder = makeBuilder(machine);
        int uniqueID = IDS.computeIfAbsent(this.id, id -> new HashMap<>()).computeIfAbsent(machine, m -> 0);
        IDS.get(this.id).put(machine, uniqueID + 1);
        this.id(new ResourceLocation("kubejs", this.id.getPath() + "/" + machine.getNamespace() + "/" + machine.getPath() + "/" + uniqueID));
    }

    @Override
    public Recipe<?> createRecipe() {
        return this.builder.build(getOrCreateId());
    }

    @Override
    public void deserialize(boolean merge) {
        DataResult<T> result = this.builderCodec.read(JsonOps.INSTANCE, this.json);
        this.builder = result.resultOrPartial(ScriptType.SERVER.console::error).orElseThrow(() -> new RecipeExceptionJS("Invalid Custom Machine Recipe"));
    }

    @Override
    public void serialize() {
        DataResult<JsonElement> result = this.builderCodec.encodeStart(JsonOps.INSTANCE, this.builder);
        this.json = (JsonObject) result.resultOrPartial(ScriptType.SERVER.console::error).orElseThrow(() -> new RecipeExceptionJS("Invalid Custom Machine Recipe"));
    }

    @Override
    public String getFromToString() {
        return this.builder.toString();
    }

    public AbstractRecipeJSBuilder<T> jei() {
        this.jei = true;
        return this;
    }

    public AbstractRecipeJSBuilder<T> priority(int priority) {
        if(!this.jei)
            this.builder.withPriority(priority);
        else
            this.builder.withJeiPriority(priority);
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
        this.builder.hide();
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
            this.builder().withRequirement(requirement);
        else
            this.builder().withJeiRequirement(requirement);
        return this;
    }
}

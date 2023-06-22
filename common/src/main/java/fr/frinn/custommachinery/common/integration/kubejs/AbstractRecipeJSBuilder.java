package fr.frinn.custommachinery.common.integration.kubejs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.recipe.IngredientMatch;
import dev.latvian.mods.kubejs.recipe.ItemInputTransformer;
import dev.latvian.mods.kubejs.recipe.ItemOutputTransformer;
import dev.latvian.mods.kubejs.recipe.RecipeArguments;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.script.ScriptType;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.crafting.IRecipeBuilder;
import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.IChanceableRequirement;
import fr.frinn.custommachinery.api.requirement.IDelayedRequirement;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractRecipeJSBuilder<T extends IRecipeBuilder<? extends Recipe<?>>> extends RecipeJS implements RecipeJSBuilder {

    private static final Map<ResourceLocation, Map<ResourceLocation, Integer>> IDS = new HashMap<>();

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

    public abstract T makeBuilder(ResourceLocation machine, RecipeArguments args);

    public T builder() {
        return this.builder;
    }

    @Override
    public void create(RecipeArguments args) {
        if(args.size() < 1 || !(args.get(0) instanceof String) || !Utils.isResourceNameValid((String)args.get(0)))
            throw new RecipeExceptionJS("Custom Machine recipe must have a machine id specified as first argument");
        this.machine = new ResourceLocation(args.getString(0, ""));
        this.builder = makeBuilder(this.machine, args);
        int uniqueID = IDS.computeIfAbsent(this.id, id -> new HashMap<>()).computeIfAbsent(this.machine, m -> 0);
        IDS.get(this.id).put(this.machine, uniqueID + 1);
        this.id(new ResourceLocation("kubejs", this.id.getPath() + "/" + this.machine.getNamespace() + "/" + this.machine.getPath() + "/" + uniqueID));
    }

    @Override
    public Recipe<?> createRecipe() {
        return this.builder.build(getOrCreateId());
    }

    @Override
    public boolean hasInput(IngredientMatch match) {
        return false;
    }

    @Override
    public boolean hasOutput(IngredientMatch match) {
        return false;
    }

    @Override
    public boolean replaceInput(IngredientMatch match, Ingredient with, ItemInputTransformer transformer) {
        return false;
    }

    @Override
    public boolean replaceOutput(IngredientMatch match, ItemStack with, ItemOutputTransformer transformer) {
        return false;
    }

    @Override
    public void deserialize() {
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

package fr.frinn.custommachinery.common.integration.kubejs;

import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentValue;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.util.HideFromJS;
import fr.frinn.custommachinery.api.crafting.IRecipeBuilder;
import fr.frinn.custommachinery.api.integration.jei.DisplayInfoTemplate;
import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RecipeRequirement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.slf4j.helpers.MessageFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class AbstractRecipeJSBuilder<B extends IRecipeBuilder<? extends Recipe<?>>> extends KubeRecipe implements RecipeJSBuilder {

    public static final Map<ResourceLocation, Map<ResourceLocation, Integer>> IDS = Collections.synchronizedMap(new HashMap<>());
    public final ResourceLocation typeID;
    private RecipeRequirement<?, ?> lastRequirement;
    public boolean jei = false;

    public AbstractRecipeJSBuilder(ResourceLocation typeID) {
        this.typeID = typeID;
    }

    public AbstractRecipeJSBuilder<B> jei() {
        this.jei = true;
        return this;
    }

    public AbstractRecipeJSBuilder<B> priority(Context cx, int priority) {
        if(!this.jei)
            set(cx, "priority", priority);
        else
            set(cx, "jeiPriority", priority);
        return this;
    }

    public AbstractRecipeJSBuilder<B> chance(double chance) {
        if(this.lastRequirement == null)
            this.error("Can't set chance before adding requirements");

        this.lastRequirement.setChance(chance);
        return this;
    }

    public AbstractRecipeJSBuilder<B> info(Consumer<DisplayInfoTemplate> consumer) {
        if(this.lastRequirement == null)
            this.error("Can't add info before adding requirements !");

        try {
            DisplayInfoTemplate template = new DisplayInfoTemplate();
            consumer.accept(template);
            this.lastRequirement.info = template;
        } catch (Exception e) {
            this.error("Error when adding custom display info on requirement {}\n{}", this.lastRequirement, e);
        }
        return this;
    }

    public AbstractRecipeJSBuilder<B> delay(double delay) {
        if(this.lastRequirement == null)
            this.error("Can't set delay before adding requirements");

        this.lastRequirement.setDelay(delay);
        return this;
    }

    @HideFromJS
    @Override
    public AbstractRecipeJSBuilder<B> addRequirement(IRequirement<?> requirement) {
        this.lastRequirement = new RecipeRequirement<>(requirement);
        for(RecipeComponentValue<?> value : this.getRecipeComponentValues()) {
            if(value.key.name.equals("requirements") && !this.jei)
                setValue((RecipeKey)value.key, addToList("requirements", this.lastRequirement));
            else if(value.key.name.equals("jei") && this.jei)
                setValue((RecipeKey)value.key, addToList("jei", this.lastRequirement));
        }
        return this;
    }

    @HideFromJS
    @Override
    public RecipeJSBuilder error(String error, Object... args) {
        throw new KubeRuntimeException(MessageFormatter.arrayFormat(error, args).getMessage()).source(this.sourceLine);
    }

    @HideFromJS
    protected <E> List<E> addToList(String key, E element) {
        List<E> list = new ArrayList<>((List<E>)get(key));
        list.add(element);
        return list;
    }

    @HideFromJS
    @Override
    public ResourceLocation getOrCreateId() {
        if(this.id == null) {
            ResourceLocation machine = (ResourceLocation) this.get("machine");
            if(machine == null)
                return super.getOrCreateId();
            int uniqueID = IDS.computeIfAbsent(this.typeID, id -> Collections.synchronizedMap(new HashMap<>())).computeIfAbsent(machine, m -> 0);
            IDS.get(this.typeID).put(machine, uniqueID + 1);
            this.id = ResourceLocation.fromNamespaceAndPath("kubejs", this.typeID.getPath() + "/" + machine.getNamespace() + "/" + machine.getPath() + "/" + uniqueID);
        }
        return this.id;
    }
}

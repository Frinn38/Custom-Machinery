package fr.frinn.custommachinery.api.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.crafting.ComponentNotFoundException;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.integration.jei.DisplayInfoTemplate;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.client.integration.jei.RequirementDisplayInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class RecipeRequirement<C extends IMachineComponent, R extends IRequirement<C>> {

    public static final NamedCodec<RecipeRequirement<?, ?>> CODEC = NamedCodec.record(recipeRequirementInstance ->
            recipeRequirementInstance.group(
                    IRequirement.CODEC.forGetter(RecipeRequirement::requirement),
                    NamedCodec.doubleRange(0.0D, 1.0D).optionalFieldOf("chance", 1.0D).forGetter(requirement -> requirement.chance),
                    NamedCodec.doubleRange(0.0D, 1.0D).optionalFieldOf("delay", 0.0D).forGetter(requirement -> requirement.delay),
                    DisplayInfoTemplate.CODEC.optionalFieldOf("info").forGetter(requirement -> Optional.ofNullable(requirement.info))
            ).apply(recipeRequirementInstance, (requirement, chance, delay, info) ->
                    new RecipeRequirement<>(requirement, chance, delay, info.orElse(null))
            ), "Recipe requirement"
    );

    private final R requirement;
    private double chance;
    private double delay;
    @Nullable
    public DisplayInfoTemplate info;

    public RecipeRequirement(R requirement, double chance, double delay, @Nullable DisplayInfoTemplate info) {
        this.requirement = requirement;
        this.chance = chance;
        this.delay = delay;
        this.info = info;
    }

    public RecipeRequirement(R requirement) {
        this(requirement, 1.0D, 0.0D, null);
    }

    @SuppressWarnings("unchecked")
    public RequirementType<R> getType() {
        return (RequirementType<R>) this.requirement.getType();
    }

    public R requirement() {
        return this.requirement;
    }

    public double chance() {
        return this.chance;
    }

    public void setChance(double chance) {
        this.chance = Mth.clamp(chance, 0.0D, 1.0D);
    }

    public double delay() {
        return this.delay;
    }

    public void setDelay(double delay) {
        this.delay = Mth.clamp(delay, 0.0D, 1.0D);
    }

    public C findComponent(IMachineComponentManager manager, ICraftingContext context) {
        return manager.getComponent(this.requirement.getComponentType()).orElseThrow(() -> new ComponentNotFoundException(context.getRecipeId(), context.getMachineTile().getMachine(), requirement.getType()));
    }

    public CraftingResult test(IMachineComponentManager manager, ICraftingContext context) {
        return this.requirement.test(findComponent(manager, context), context) ? CraftingResult.success() : CraftingResult.error(Component.empty());
    }

    public boolean shouldSkip(IMachineComponentManager manager, Random rand, ICraftingContext context) {
        double chance = context.getModifiedValue(this.chance, this.requirement, "chance");
        return rand.nextDouble() > chance;
    }

    public boolean isDelayed() {
        return this.delay > 0.0D && this.delay < 1.0D;
    }

    public void getDisplayInfo(RequirementDisplayInfo info) {
        this.requirement.getDefaultDisplayInfo(info, this);
    }

    public List<? extends IJEIIngredientWrapper<?>> getJeiIngredientWrappers(IMachineRecipe recipe) {
        if(this.requirement instanceof IJEIIngredientRequirement<?> ingredientRequirement)
            return ingredientRequirement.getJEIIngredientWrappers(recipe, this);
        return Collections.emptyList();
    }
}

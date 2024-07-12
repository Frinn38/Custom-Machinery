package fr.frinn.custommachinery.impl.crafting;

import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IRequirementList;
import fr.frinn.custommachinery.api.requirement.RecipeRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequirementList<C extends IMachineComponent> implements IRequirementList<C> {

    private final Map<Double, List<RequirementWithFunction>> processRequirements = new HashMap<>();
    private final List<RequirementWithFunction> tickableRequirements = new ArrayList<>();
    private final List<RequirementWithFunction> worldConditions = new ArrayList<>();
    private final List<RequirementWithFunction> inventoryConditions = new ArrayList<>();

    private RecipeRequirement<? extends IMachineComponent, ?> currentRequirement;

    public void setCurrentRequirement(RecipeRequirement<?, ?> requirement) {
        this.currentRequirement = requirement;
    }

    @Override
    public void processOnStart(RequirementFunction<C> function) {
        this.processRequirements.computeIfAbsent(1.0D, delay -> new ArrayList<>()).add(new RequirementWithFunction(this.currentRequirement, function));
    }

    @Override
    public void processOnEnd(RequirementFunction<C> function) {
        this.processRequirements.computeIfAbsent(0.0D, delay -> new ArrayList<>()).add(new RequirementWithFunction(this.currentRequirement, function));
    }

    @Override
    public void processEachTick(RequirementFunction<C> function) {
        this.tickableRequirements.add(new RequirementWithFunction(this.currentRequirement, function));
    }

    @Override
    public void worldCondition(RequirementFunction<C> function) {
        this.worldConditions.add(new RequirementWithFunction(this.currentRequirement, function));
    }

    @Override
    public void inventoryCondition(RequirementFunction<C> function) {
        this.inventoryConditions.add(new RequirementWithFunction(this.currentRequirement, function));
    }

    @Override
    public void processDelayed(double baseDelay, RequirementFunction<C> function) {
        this.processRequirements.computeIfAbsent(baseDelay, delay -> new ArrayList<>()).add(new RequirementWithFunction(this.currentRequirement, function));
    }

    @Override
    public void process(RequirementIOMode mode, RequirementFunction<C> function) {
        this.processDelayed(mode == RequirementIOMode.INPUT ? 0.0D : 1.0D, function);
    }

    public List<RequirementWithFunction> getWorldConditions() {
        return this.worldConditions;
    }

    public List<RequirementWithFunction> getInventoryConditions() {
        return this.inventoryConditions;
    }

    public Map<Double, List<RequirementWithFunction>> getProcessRequirements() {
        return this.processRequirements;
    }

    public List<RequirementWithFunction> getTickableRequirements() {
        return this.tickableRequirements;
    }

    public record RequirementWithFunction(RecipeRequirement<?, ?> requirement, RequirementFunction<?> function) {
        @SuppressWarnings({"rawtypes", "unchecked"})
        public CraftingResult process(IMachineComponentManager manager, ICraftingContext context) {
            return ((RequirementFunction)this.function).process(this.requirement.findComponent(manager, context), context);
        }
    }
}

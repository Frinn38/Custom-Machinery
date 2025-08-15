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
        this.addProcessRequirement(0.0D, function);
    }

    @Override
    public void processOnEnd(RequirementFunction<C> function) {
        this.addProcessRequirement(1.0D, function);
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
        this.addProcessRequirement(baseDelay, function);
    }

    @Override
    public void process(RequirementIOMode mode, RequirementFunction<C> function) {
        this.addProcessRequirement(mode == RequirementIOMode.INPUT ? 0.0D : 1.0D, function);
    }

    /**
     * Handle choosing whether the requirement will use the base delay (0 for inputs and 1 for outputs) or a user specified custom delay.
     * We know the user specified a custom delay when {@link RecipeRequirement#delay()} returns something other than -1 (default value).
     * @param baseDelay The default delay specified by the requirement.
     * @param function The {@link fr.frinn.custommachinery.api.crafting.IRequirementList.RequirementFunction} to use when the process reach the specified delay.
     */
    private void addProcessRequirement(double baseDelay, RequirementFunction<C> function) {
        double delay = this.currentRequirement.delay();
        if(delay == -1.0D) //Default value so use the default delay
            delay = baseDelay;
        else //Custom value so use it
            delay = Math.clamp(delay, 0.0D, 1.0D); //Clamp as the user might have specified an incorrect delay value
        this.processRequirements.computeIfAbsent(delay, d -> new ArrayList<>()).add(new RequirementWithFunction(this.currentRequirement, function));
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

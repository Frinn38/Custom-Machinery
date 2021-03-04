package fr.frinn.custommachinery.common.crafting;

import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.crafting.requirements.ITickableRequirement;
import fr.frinn.custommachinery.common.data.component.IMachineComponent;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CraftingManager {

    private CustomMachineTile tile;
    private CustomMachineRecipe currentRecipe;
    public int recipeProgressTime = 0;
    public int recipeTotalTime = 0; //For client GUI only
    private List<IRequirement> processedRequirements;

    private STATUS status;
    private PHASE phase;

    private ITextComponent errorMessage;
    private final ITextComponent IDLE_MESSAGE = new StringTextComponent("idle");
    private final ITextComponent RUNNING_MESSAGE = new StringTextComponent("running");



    public CraftingManager(CustomMachineTile tile) {
        this.tile = tile;
        this.status = STATUS.IDLE;
        this.processedRequirements = new ArrayList<>();
    }

    public void tick() {
        if(this.currentRecipe == null) {
            this.recipeProgressTime = 0;
            CustomMachineRecipe recipe = this.findRecipe();
            if(recipe != null) {
                this.currentRecipe = recipe;
                this.recipeTotalTime = recipe.getRecipeTime();
                this.phase = PHASE.STARTING;
                this.setRunning();
            }
        } else {
            switch (this.phase) {
                case STARTING:
                    for(IRequirement requirement : this.currentRecipe.getRequirements()) {
                        if(!this.processedRequirements.contains(requirement)) {
                            IMachineComponent component = this.tile.componentManager.getComponent(requirement.getComponentType()).get();
                            CraftingResult result = requirement.processStart(component);
                            if(!result.isSuccess()) {
                                this.setErrored(result.getMessage());
                                break;
                            }
                            else this.processedRequirements.add(requirement);
                        }
                    }

                    if(this.processedRequirements.size() == this.currentRecipe.getRequirements().size()) {
                        this.setRunning();
                        this.phase = PHASE.CRAFTING;
                        this.processedRequirements.clear();
                    }
                    break;
                case CRAFTING:
                    List<ITickableRequirement> tickableRequirements = this.currentRecipe.getRequirements()
                            .stream()
                            .filter(requirement -> requirement instanceof ITickableRequirement)
                            .map(requirement -> (ITickableRequirement)requirement)
                            .collect(Collectors.toList());

                    for (ITickableRequirement tickableRequirement : tickableRequirements) {
                        if(!this.processedRequirements.contains(tickableRequirement)) {
                            CraftingResult result = tickableRequirement.processTick(this.tile.componentManager.getComponent(tickableRequirement.getComponentType()).get());
                            if(!result.isSuccess()) {
                                this.setErrored(result.getMessage());
                                break;
                            }
                            else
                                this.processedRequirements.add(tickableRequirement);
                        }
                    }

                    if(this.processedRequirements.size() == tickableRequirements.size()) {
                        this.recipeProgressTime++;
                        this.setRunning();
                        this.processedRequirements.clear();
                    }
                    if(this.recipeProgressTime == this.currentRecipe.getRecipeTime())
                        this.phase = PHASE.ENDING;
                    break;
                case ENDING:
                    for(IRequirement requirement : this.currentRecipe.getRequirements()) {
                        if(!this.processedRequirements.contains(requirement)) {
                            CraftingResult result = requirement.processEnd(this.tile.componentManager.getComponentRaw(requirement.getComponentType()));
                            if(!result.isSuccess()) {
                                this.setErrored(result.getMessage());
                                break;
                            }
                            else
                                this.processedRequirements.add(requirement);
                        }
                    }

                    if(this.processedRequirements.size() == this.currentRecipe.getRequirements().size()) {
                        this.currentRecipe = null;
                        this.setIdle();
                        this.processedRequirements.clear();
                    }
                    break;
            }
        }

    }

    private CustomMachineRecipe findRecipe() {
        return this.tile.getWorld().getRecipeManager()
                .getRecipesForType(Registration.CUSTOM_MACHINE_RECIPE)
                .stream()
                .filter(recipe -> recipe.getMachine().equals(this.tile.getMachine().getId()))
                .filter(recipe -> recipe.matches(this.tile))
                .findFirst()
                .orElse(null);
    }

    public ITextComponent getMessage() {
        switch (this.status) {
            case IDLE:
                return this.IDLE_MESSAGE;
            case RUNNING:
                return this.RUNNING_MESSAGE;
            case ERRORED:
                return this.errorMessage;
            default:
                return this.errorMessage;
        }
    }

    public void setIdle() {
        if(this.status != STATUS.IDLE) {
            this.status = STATUS.IDLE;
            this.tile.markForSyncing();
        }
    }

    public void setErrored(ITextComponent message) {
        if(this.status != STATUS.ERRORED || !this.errorMessage.equals(message)) {
            this.status = STATUS.ERRORED;
            this.errorMessage = message;
            if (this.tile.getWorld() != null && !this.tile.getWorld().isRemote())
                this.tile.markForSyncing();
        }
    }

    public void setRunning() {
        if(this.status != STATUS.RUNNING) {
            this.status = STATUS.RUNNING;
            if (this.tile.getWorld() != null && !this.tile.getWorld().isRemote())
                this.tile.markForSyncing();
        }
    }

    public STATUS getStatus() {
        return this.status;
    }

    public CustomMachineRecipe getCurrentRecipe() {
        return this.currentRecipe;
    }

    public enum PHASE {
        STARTING,
        CRAFTING,
        ENDING,
    }

    public enum STATUS {
        IDLE,
        RUNNING,
        ERRORED;

        public static STATUS value(String string) {
            return valueOf(string.toUpperCase(Locale.ENGLISH));
        }
    }
}

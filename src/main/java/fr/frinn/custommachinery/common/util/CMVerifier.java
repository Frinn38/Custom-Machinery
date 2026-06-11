package fr.frinn.custommachinery.common.util;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.guielement.IComponentGuiElement;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.requirement.RecipeRequirement;
import fr.frinn.custommachinery.common.component.item.ItemMachineComponent;
import fr.frinn.custommachinery.common.crafting.craft.CustomCraftRecipe;
import fr.frinn.custommachinery.common.crafting.machine.CustomMachineRecipe;
import fr.frinn.custommachinery.common.guielement.SlotGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.upgrade.MachineUpgrade;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.LinkedList;
import java.util.List;

public class CMVerifier {

    public static Result verify(RecipeManager manager) {
        ResultBuilder builder = new ResultBuilder();
        builder.info("---------------------------------------------");
        builder.info("| Starting verification of Custom Machinery |");
        builder.info("---------------------------------------------");

        //Machines
        builder.pushCategory("Machines");
        builder.info("Found {} custom machines : ", CustomMachinery.MACHINES.size());
        CustomMachinery.MACHINES.forEach((id, machine) -> {
            builder.pushCategory(machine.getId().toString());
            verifyMachine(builder, machine);
            builder.appendNoErrorMessage();
            builder.popCategory();
        });
        builder.popCategory();

        //Upgrades
        builder.pushCategory("Upgrades");
        builder.info("Found {} custom machine upgrades", CustomMachinery.UPGRADES.getAllUpgrades().size());
        CustomMachinery.UPGRADES.getAllUpgrades().forEach((location, upgrade) -> {
            builder.pushCategory(BuiltInRegistries.ITEM.getKey(upgrade.item()).toString());
            verifyUpgrade(builder, upgrade);
            builder.appendNoErrorMessage();
            builder.popCategory();
        });
        builder.popCategory();

        //Machine recipes
        builder.pushCategory("Recipes");
        List<RecipeHolder<CustomMachineRecipe>> machineRecipes = manager.getAllRecipesFor(Registration.CUSTOM_MACHINE_RECIPE.get());
        builder.info("Found {} custom machine recipes", machineRecipes.size());
        machineRecipes.forEach(holder -> {
            builder.pushCategory(holder.id().toString());
            verifyMachineRecipe(builder, holder.value());
            builder.appendNoErrorMessage();
            builder.popCategory();
        });

        //Craft recipes
        List<RecipeHolder<CustomCraftRecipe>> craftRecipes = manager.getAllRecipesFor(Registration.CUSTOM_CRAFT_RECIPE.get());
        builder.info("Found {} custom craft recipes", craftRecipes.size());
        craftRecipes.forEach(holder -> {
            builder.pushCategory(holder.id().toString());
            verifyCraftRecipe(builder, holder.value());
            builder.appendNoErrorMessage();
            builder.popCategory();
        });
        builder.popCategory();

        return builder.build();
    }

    public static void verifyMachine(ResultBuilder builder, CustomMachine machine) {
        //Checking duplicate components
        for(IMachineComponentTemplate<?> template : machine.getComponentTemplates())
            for(IMachineComponentTemplate<?> other : machine.getComponentTemplates())
                if(template != other && template.getType() == other.getType() && template.getId().equals(other.getId()))
                    builder.error("Multiple machine components of type {} have the same id '{}'", template.getType().getId().toString(), template.getId());

        //Checking duplicate elements
        for(IGuiElement element : machine.getGuiElements())
            for(IGuiElement other : machine.getGuiElements())
                if(element != other && element.getType() == other.getType() && element.getId().equals(other.getId()) && !element.getId().isEmpty())
                    builder.error("Multiple gui elements of type {} have the same id '{}'", element.getType().getId().toString(), element.getId());

        //Checking component gui elements without components
        for(IGuiElement element : machine.getGuiElements()) {
            if(element instanceof SlotGuiElement slotGuiElement && machine.getComponentTemplates().stream().noneMatch(template -> template instanceof ItemMachineComponent.Template && template.getId().equals(slotGuiElement.getComponentId())))
                builder.error("Slot gui element of id '{}' doesn't have an associated machine component", slotGuiElement.getComponentId());
            if(element.getType() != Registration.SLOT_GUI_ELEMENT.get() && element instanceof IComponentGuiElement<?> componentGuiElement && machine.getComponentTemplates().stream().noneMatch(template -> template.getType() == componentGuiElement.getComponentType() && template.getId().equals(componentGuiElement.getComponentId())))
                builder.error("Gui element of type {} and id '{}' doesn't have an associated machine component", componentGuiElement.getComponentType().getId().toString(), componentGuiElement.getComponentId());
        }

        //Checking presence of result item component
        boolean crafter = machine.getProcessorTemplate().getType() == Registration.CRAFT_PROCESSOR.get();
        for(IMachineComponentTemplate<?> template : machine.getComponentTemplates())
            if(template.getType() == Registration.ITEM_RESULT_MACHINE_COMPONENT.get() && !crafter)
                builder.error("Found item component of type {} with id '{}' but machine isn't using craft processor !\n" +
                        "Result item components should only be used for machines with craft processor, consider using item component type {} instead.", template.getType().getId().toString(), template.getId(), Registration.ITEM_MACHINE_COMPONENT.get().getId().toString());
        if(crafter && machine.getComponentTemplates().stream().noneMatch(template -> template.getType() == Registration.ITEM_RESULT_MACHINE_COMPONENT.get()))
            builder.error("No result item machine component found but machine is using craft processor !\n" +
                    "At least 1 item component of type {} is required for the craft processor to work correctly.", Registration.ITEM_RESULT_MACHINE_COMPONENT.get().getId().toString());
    }

    private static void verifyUpgrade(ResultBuilder builder, MachineUpgrade upgrade) {
        //Check that machine exists
        for(ResourceLocation id : upgrade.machines())
            if(!CustomMachinery.MACHINES.containsKey(id))
                builder.error("Unknown machine id {} specified for this upgrade", id.toString());

        //Check that upgrade has recipeModifiers
        if(upgrade.recipeModifiers().isEmpty())
            builder.error("Upgrade doesn't have any recipeModifiers");
    }

    private static void verifyMachineRecipe(ResultBuilder builder, CustomMachineRecipe recipe) {
        //Check that the machine exists
        CustomMachine machine = CustomMachinery.MACHINES.get(recipe.getMachineId());
        if(machine == null) {
            builder.error("Unknown machine id: {}", recipe.getMachineId());
            return;
        }

        //Check that the machine has the correct processor
        if(machine.getProcessorTemplate().getType() != Registration.MACHINE_PROCESSOR.get())
            builder.error("Recipe can't be processed by machine {} as it doesn't use machine processor");

        //Check that the recipe has requirements
        if(recipe.getRequirements().isEmpty() && recipe.getJeiRequirements().isEmpty())
            builder.error("Recipe doesn't have any requirements");

        //Check requirements
        for(RecipeRequirement<?, ?> requirement : recipe.getRequirements())
            if(!requirement.requirement().getComponentType().isDefaultComponent() && machine.getComponentTemplates().stream().noneMatch(template -> template.getType() == requirement.requirement().getComponentType()))
                builder.error("Recipe has a requirement of type {} but machine doesn't have a component of type {}", requirement.requirement().getType().getId().toString(), requirement.requirement().getComponentType().getId().toString());
    }

    private static void verifyCraftRecipe(final ResultBuilder builder, CustomCraftRecipe recipe) {
        //Check that the machine exists
        CustomMachine machine = CustomMachinery.MACHINES.get(recipe.getMachineId());
        if(machine == null) {
            builder.error("Unknown machine id: {}", recipe.getMachineId());
            return;
        }

        //Check that the machine has the correct processor
        if(machine.getProcessorTemplate().getType() != Registration.CRAFT_PROCESSOR.get()) {
            builder.error("Recipe can't be processed by machine {} as it doesn't use craft processor");
        }

        //Check that the recipe has requirements
        if(recipe.getRequirements().isEmpty() && recipe.getJeiRequirements().isEmpty())
            builder.error("Recipe doesn't have any requirements");

        //Check requirements
        for(RecipeRequirement<?, ?> requirement : recipe.getRequirements())
            if(!requirement.requirement().getComponentType().isDefaultComponent() && machine.getComponentTemplates().stream().noneMatch(template -> template.getType() == requirement.requirement().getComponentType()))
                builder.error("Recipe has a requirement of type {} but machine doesn't have a component of type {}", requirement.requirement().getType().getId().toString(), requirement.requirement().getComponentType().getId().toString());
    }

    public record LogLine(Level level, Marker category, String message, Object... args) {}

    public record Result(int errors, List<LogLine> log) {

        public void print(Logger logger) {
            this.log.forEach(line -> logger.log(line.level, line.category, line.message, line.args));
        }

        public List<String> getErrors() {
            Logger logger = ICustomMachineryAPI.INSTANCE.logger();
            return this.log.stream().filter(line -> line.level == Level.ERROR).map(line -> logger.getMessageFactory().newMessage(line.message, line.args).getFormattedMessage()).toList();
        }
    }

    public static class ResultBuilder {
        private final List<LogLine> log = new LinkedList<>();
        private int errors = 0;
        private Marker category = MarkerManager.getMarker("Verifier");
        private boolean errorInCurrentCategory = false;

        public void pushCategory(String category) {
            Marker newCategory = MarkerManager.getMarker(this.category.getName() + "/" + category);
            newCategory.addParents(this.category);
            this.category = newCategory;
            this.errorInCurrentCategory = false;
        }

        public void popCategory() {
            if(this.category == null || this.category.getParents() == null)
                throw new IllegalStateException("Popping too much !");
            this.category = this.category.getParents()[0];
        }

        public void info(String message, Object... args) {
            this.log.add(new LogLine(Level.INFO, this.category, message, args));
        }

        public void error(String message, Object... args) {
            this.log.add(new LogLine(Level.ERROR, this.category, message, args));
            this.errors++;
            this.errorInCurrentCategory = true;
        }

        public void appendNoErrorMessage() {
            if(!this.errorInCurrentCategory)
                this.info("No error found");
        }

        public Result build() {
            return new Result(this.errors, this.log);
        }
    }
}

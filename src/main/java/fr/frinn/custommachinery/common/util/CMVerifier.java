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
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CMVerifier {

    public static int verify(RecipeManager manager) {
        final Logger logger = ICustomMachineryAPI.INSTANCE.logger();
        AtomicInteger errors = new AtomicInteger();
        logger.info("---------------------------------------------");
        logger.info("| Starting verification of Custom Machinery |");
        logger.info("---------------------------------------------");
        //Machines
        logger.info("Found {} custom machines : ", CustomMachinery.MACHINES.size());
        CustomMachinery.MACHINES.forEach((id, machine) -> {
            logger.info(" - {}", id.toString());
            errors.addAndGet(verifyMachine(logger, machine));
        });

        //Upgrades
        logger.info("Found {} custom machine upgrades", CustomMachinery.UPGRADES.getAllUpgrades().size());
        CustomMachinery.UPGRADES.getAllUpgrades().forEach((location, upgrade) -> {
            logger.info(" - {}", BuiltInRegistries.ITEM.getKey(upgrade.item()).toString());
            errors.addAndGet(verifyUpgrade(logger, upgrade));
        });

        //Machine recipes
        List<RecipeHolder<CustomMachineRecipe>> machineRecipes = manager.getAllRecipesFor(Registration.CUSTOM_MACHINE_RECIPE.get());
        logger.info("Found {} custom machine recipes", machineRecipes.size());
        machineRecipes.forEach(holder -> {
            logger.info(" - {}", holder.id().toString());
            errors.addAndGet(verifyMachineRecipe(logger, holder.value()));
        });

        //Craft recipes
        List<RecipeHolder<CustomCraftRecipe>> craftRecipes = manager.getAllRecipesFor(Registration.CUSTOM_CRAFT_RECIPE.get());
        logger.info("Found {} custom craft recipes", craftRecipes.size());
        craftRecipes.forEach(holder -> {
            logger.info(" - {}", holder.id().toString());
            errors.addAndGet(verifyCraftRecipe(logger, holder.value()));
        });
        return errors.get();
    }

    private static int verifyMachine(final Logger logger, CustomMachine machine) {
        int errors = 0;
        //Checking duplicate components
        for(IMachineComponentTemplate<?> template : machine.getComponentTemplates()) {
            for(IMachineComponentTemplate<?> other : machine.getComponentTemplates()) {
                if(template != other && template.getType() == other.getType() && template.getId().equals(other.getId())) {
                    logger.error(" - Multiple machine components of type {} have the same id '{}'", template.getType().getId().toString(), template.getId());
                    errors++;
                }
            }
        }

        //Checking duplicate elements
        for(IGuiElement element : machine.getGuiElements()) {
            for(IGuiElement other : machine.getGuiElements()) {
                if(element != other && element.getType() == other.getType() && element.getId().equals(other.getId()) && !element.getId().isEmpty()) {
                    logger.error(" - Multiple gui elements of type {} have the same id '{}'", element.getType().getId().toString(), element.getId());
                    errors++;
                }
            }
        }

        //Checking component gui elements without components
        for(IGuiElement element : machine.getGuiElements()) {
            if(element instanceof SlotGuiElement slotGuiElement && machine.getComponentTemplates().stream().noneMatch(template -> template instanceof ItemMachineComponent.Template && template.getId().equals(slotGuiElement.getComponentId())))
                logger.error(" - Slot gui element of id '{}' doesn't have an associated machine component", slotGuiElement.getComponentType().getId().toString(), slotGuiElement.getComponentId());
            if(element.getType() != Registration.SLOT_GUI_ELEMENT.get() && element instanceof IComponentGuiElement<?> componentGuiElement && machine.getComponentTemplates().stream().noneMatch(template -> template.getType() == componentGuiElement.getComponentType() && template.getId().equals(componentGuiElement.getComponentId())))
                logger.error(" - Gui element of type {} and id '{}' doesn't have an associated machine component", componentGuiElement.getComponentType().getId().toString(), componentGuiElement.getComponentId());
        }

        //Checking presence of result item component
        boolean crafter = machine.getProcessorTemplate().getType() == Registration.CRAFT_PROCESSOR.get();
        for(IMachineComponentTemplate<?> template : machine.getComponentTemplates()) {
            if(template.getType() == Registration.ITEM_RESULT_MACHINE_COMPONENT.get() && !crafter) {
                logger.error(" - Found item component of type {} with id '{}' but machine isn't using craft processor !\n" +
                        "Result item components should only be used for machines with craft processor, consider using item component type {} instead.", template.getType().getId().toString(), template.getId(), Registration.ITEM_MACHINE_COMPONENT.get().getId().toString());
                errors++;
            }
        }
        if(crafter && machine.getComponentTemplates().stream().noneMatch(template -> template.getType() == Registration.ITEM_RESULT_MACHINE_COMPONENT.get())) {
            logger.error(" - No result item machine component found but machine is using craft processor !\n" +
                    "At least 1 item component of type {} is required for the craft processor to work correctly.", Registration.ITEM_RESULT_MACHINE_COMPONENT.get().getId().toString());
            errors++;
        }
        return errors;
    }

    private static int verifyUpgrade(final Logger logger, MachineUpgrade upgrade) {
        int errors = 0;
        //Check that machine exists
        for(ResourceLocation id : upgrade.machines()) {
            if(!CustomMachinery.MACHINES.containsKey(id)) {
                logger.error(" - Unknown machine id {} specified for this upgrade", id.toString());
                errors++;
            }
        }

        //Check that upgrade has recipeModifiers
        if(upgrade.recipeModifiers().isEmpty()) {
            logger.error(" - Upgrade doesn't have any recipeModifiers");
            errors++;
        }
        return errors;
    }

    private static int verifyMachineRecipe(final Logger logger, CustomMachineRecipe recipe) {
        int errors = 0;
        //Check that the machine exists
        CustomMachine machine = CustomMachinery.MACHINES.get(recipe.getMachineId());
        if(machine == null) {
            logger.error(" - Unknown machine id: {}", recipe.getMachineId());
            return 1;
        }

        //Check that the machine has the correct processor
        if(machine.getProcessorTemplate().getType() != Registration.MACHINE_PROCESSOR.get()) {
            logger.error(" - Recipe can't be processed by machine {} as it doesn't use machine processor");
            errors++;
        }

        //Check that the recipe has requirements
        if(recipe.getRequirements().isEmpty() && recipe.getJeiRequirements().isEmpty()) {
            logger.error(" - Recipe doesn't have any requirements");
            errors++;
        }

        //Check requirements
        for(RecipeRequirement<?, ?> requirement : recipe.getRequirements()) {
            if(!requirement.requirement().getComponentType().isDefaultComponent() && machine.getComponentTemplates().stream().noneMatch(template -> template.getType() == requirement.requirement().getComponentType())) {
                logger.error(" - Recipe has a requirement of type {} but machine doesn't have a component of type {}", requirement.requirement().getType().getId().toString(), requirement.requirement().getComponentType().getId().toString());
                errors++;
            }
        }
        return errors;
    }

    private static int verifyCraftRecipe(final Logger logger, CustomCraftRecipe recipe) {
        int errors = 0;
        //Check that the machine exists
        CustomMachine machine = CustomMachinery.MACHINES.get(recipe.getMachineId());
        if(machine == null) {
            logger.error(" - Unknown machine id: {}", recipe.getMachineId());
            return 1;
        }

        //Check that the machine has the correct processor
        if(machine.getProcessorTemplate().getType() != Registration.CRAFT_PROCESSOR.get()) {
            logger.error(" - Recipe can't be processed by machine {} as it doesn't use craft processor");
            errors++;
        }

        //Check that the recipe has requirements
        if(recipe.getRequirements().isEmpty() && recipe.getJeiRequirements().isEmpty()) {
            logger.error(" - Recipe doesn't have any requirements");
            errors++;
        }

        //Check requirements
        for(RecipeRequirement<?, ?> requirement : recipe.getRequirements()) {
            if(!requirement.requirement().getComponentType().isDefaultComponent() && machine.getComponentTemplates().stream().noneMatch(template -> template.getType() == requirement.requirement().getComponentType())) {
                logger.error(" - Recipe has a requirement of type {} but machine doesn't have a component of type {}", requirement.requirement().getType().getId().toString(), requirement.requirement().getComponentType().getId().toString());
                errors++;
            }
        }
        return errors;
    }
}

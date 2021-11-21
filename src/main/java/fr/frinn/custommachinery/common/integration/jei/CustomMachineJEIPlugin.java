package fr.frinn.custommachinery.common.integration.jei;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.common.data.gui.ProgressBarGuiElement;
import fr.frinn.custommachinery.common.init.CustomMachineItem;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.energy.EnergyIngredientHelper;
import fr.frinn.custommachinery.common.util.Comparators;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.MethodsReturnNonnullByDefault;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@JeiPlugin
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CustomMachineJEIPlugin implements IModPlugin {

    public static final ResourceLocation PLUGIN_ID = new ResourceLocation(CustomMachinery.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.useNbtForSubtypes(Registration.CUSTOM_MACHINE_ITEM.get());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        CustomMachinery.MACHINES.forEach((id, machine) -> registry.addRecipeCategories(new CustomMachineRecipeCategory(machine, registry.getJeiHelpers().getGuiHelper())));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry) {
        if(Minecraft.getInstance().world == null)
            return;
        Minecraft.getInstance().world.getRecipeManager()
                .getRecipesForType(Registration.CUSTOM_MACHINE_RECIPE)
                .stream()
                .sorted(Comparators.JEI_PRIORITY_COMPARATOR.reversed())
                .forEach(recipe -> {
                    if(CustomMachinery.MACHINES.containsKey(recipe.getMachine()))
                        registry.addRecipes(Lists.newArrayList(recipe), recipe.getMachine());
                    else
                        CustomMachinery.LOGGER.error("Invalid machine ID: " + recipe.getMachine() + " in recipe: " + recipe.getId());
                }
        );
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registry) {
        registry.register(CustomIngredientTypes.ENERGY, new ArrayList<>(), new EnergyIngredientHelper(), new DummyIngredientRenderer<>());
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(CustomMachineScreen.class, new IGuiContainerHandler<CustomMachineScreen>() {
            @Override
            public Collection<IGuiClickableArea> getGuiClickableAreas(CustomMachineScreen screen, double mouseX, double mouseY) {
                List<IGuiElement> elements = screen.getMachine().getJeiElements().isEmpty() ? screen.getMachine().getGuiElements() : screen.getMachine().getJeiElements();
                ProgressBarGuiElement progress = (ProgressBarGuiElement) elements.stream().filter(element -> element.getType() == Registration.PROGRESS_GUI_ELEMENT.get()).findFirst().orElse(null);
                if(progress != null) {
                    int posX = progress.getX();
                    int posY = progress.getY();
                    boolean invertAxis = progress.getEmptyTexture().equals(ProgressBarGuiElement.BASE_EMPTY_TEXTURE) && progress.getFilledTexture().equals(ProgressBarGuiElement.BASE_FILLED_TEXTURE) && progress.getDirection() != ProgressBarGuiElement.Direction.RIGHT && progress.getDirection() != ProgressBarGuiElement.Direction.LEFT;
                    int width = invertAxis ? progress.getHeight() : progress.getWidth();
                    int height = invertAxis ? progress.getWidth() : progress.getHeight();
                    return Collections.singleton(IGuiClickableArea.createBasic(posX, posY, width, height, screen.getMachine().getId()));
                }
                return Collections.emptyList();
            }
        });
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        CustomMachinery.MACHINES.forEach((id, machine) -> {
            registration.addRecipeCatalyst(CustomMachineItem.makeMachineItem(id), id);
            machine.getCatalysts().stream().filter(catalyst -> CustomMachinery.MACHINES.containsKey(catalyst) && !catalyst.equals(id)).forEach(catalyst -> registration.addRecipeCatalyst(CustomMachineItem.makeMachineItem(catalyst), id));
        });
    }

    //Removed, as this won't wonrk since this method is called on the client side, and the all items modifications must happen on the server side.
    //Keeping this code for moving it on the server side.
    /*
    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        for(Map.Entry<ResourceLocation, CustomMachine> entry : CustomMachinery.MACHINES.entrySet()) {
            CustomMachine machine = entry.getValue();
            List<IGuiElement> elements = machine.getJeiElements().isEmpty() ? machine.getGuiElements() : machine.getJeiElements();
            if(machine.isDummy() || machine.getComponentTemplates().stream().noneMatch(template -> template instanceof ItemMachineComponent.Template) || elements.stream().noneMatch(element -> element instanceof SlotGuiElement))
                continue;

            registration.addRecipeTransferHandler(new IRecipeTransferHandler<CustomMachineContainer>() {
                @Override
                public Class<CustomMachineContainer> getContainerClass() {
                    return CustomMachineContainer.class;
                }

                @Nullable
                @Override
                public IRecipeTransferError transferRecipe(CustomMachineContainer container, Object recipeObject, IRecipeLayout layout, PlayerEntity player, boolean maxTransfer, boolean doTransfer) {
                    //If the recipe is not a CustomMachineRecipe something weird happened, and we return an error.
                    if(!(recipeObject instanceof CustomMachineRecipe))
                        return RecipeTransferError.INVALID_RECIPE;
                    CustomMachineRecipe recipe = (CustomMachineRecipe) recipeObject;

                    //Get a list of input item requirements
                    List<ItemRequirement> items = recipe.getRequirements()
                            .stream()
                            .filter(requirement -> requirement instanceof ItemRequirement && requirement.getMode() == IRequirement.MODE.INPUT)
                            .map(requirement -> (ItemRequirement)requirement)
                            .sorted(Comparator.comparing(requirement -> requirement.getSlot().isEmpty() ? -1 : 1))
                            .collect(Collectors.toList());
                    //If no item requirements are found we have no items to transfer, so we can return.
                    if(items.isEmpty())
                        return RecipeTransferError.INVALID_RECIPE;

                    //Available slots, remove from this list when a slot have been filled.
                    Collection<SlotItemComponent> slots = container.getInputSlots().values();

                    //Loop through all input item requirements, if they are all transferred we can return null (success), if one can't be transferred we return an error.
                    for(ItemRequirement requirement : items) {
                        //If the item must be in a specific slot we check only this specific slot.
                        if(!requirement.getSlot().isEmpty() && container.getInputSlots().containsKey(requirement.getSlot())) {
                            SlotItemComponent slot = container.getInputSlots().get(requirement.getSlot());
                            ItemStack slotStack = slot.getStack();
                            //If the item already present in the slot is the item needed by the requirement.
                            if(requirement.getItem().test(slotStack.getItem()) && Utils.testNBT(requirement.getNbt(), slotStack.getTag())) {
                                //If the stack already present in the slot doesn't have enough items, and the player have enough items in its inventory to complement we do so.
                                if(slotStack.getCount() < requirement.getAmount() && Utils.getPlayerInventoryItemStackAmount(player, requirement.getItem(), requirement.getNbt()) >= requirement.getAmount() - slotStack.getCount())
                                    Utils.moveStackFromPlayerInvToSlot(player, slot, requirement.getItem(), requirement.getAmount() - slotStack.getCount(), requirement.getNbt());
                                else //Else that mean the player don't have enough items to complete the required amount, so we return an error.
                                    return RecipeTransferError.NOT_ENOUGH_ITEM;
                            }
                            //If the item in the slot don't match the required item, we check if the player have enough items in its inventory to match the required amount.
                            else if(Utils.getPlayerInventoryItemStackAmount(player, requirement.getItem(), requirement.getNbt()) >= requirement.getAmount()) {
                                //If so, we give the content of the slot to the player (if the slot is not empty).
                                if(!slotStack.isEmpty()) {
                                    ItemHandlerHelper.giveItemToPlayer(player, slotStack);
                                    slot.putStack(ItemStack.EMPTY);
                                }
                                //Now that the slot is empty we can transfer the items from the player inventory to the slot.
                                Utils.moveStackFromPlayerInvToSlot(player, slot, requirement.getItem(), requirement.getAmount(), requirement.getNbt());
                            } else //The player don't have the required items in its inventory, so we return an error.
                                return RecipeTransferError.NOT_ENOUGH_ITEM;

                            //Remove the slot we just inserted in from the available slots.
                            slots.remove(slot);
                        }
                        //The item does not have to be in a specific slot, so we use the first available slot.
                        else if(requirement.getSlot().isEmpty()) {
                            //If no slot are available we return an error.
                            if(slots.isEmpty())
                                return RecipeTransferError.INVALID_RECIPE;
                            //Peek the first available slot and try to fill it with items from the player inventory.
                            SlotItemComponent slot = slots.iterator().next();
                            ItemStack slotStack = slot.getStack();
                            //If the item already present in the slot is the item needed by the requirement.
                            if(requirement.getItem().test(slotStack.getItem()) && Utils.testNBT(requirement.getNbt(), slotStack.getTag())) {
                                //If the stack already present in the slot doesn't have enough items, and the player have enough items in its inventory to complement we do so.
                                if(slotStack.getCount() < requirement.getAmount() && Utils.getPlayerInventoryItemStackAmount(player, requirement.getItem(), requirement.getNbt()) >= requirement.getAmount() - slotStack.getCount())
                                    Utils.moveStackFromPlayerInvToSlot(player, slot, requirement.getItem(), requirement.getAmount() - slotStack.getCount(), requirement.getNbt());
                                else //Else that mean the player don't have enough items to complete the required amount, so we return an error.
                                    return RecipeTransferError.NOT_ENOUGH_ITEM;
                            }
                            //If the item in the slot don't match the required item, we check if the player have enough items in its inventory to match the required amount.
                            else if(Utils.getPlayerInventoryItemStackAmount(player, requirement.getItem(), requirement.getNbt()) >= requirement.getAmount()) {
                                //If so, we give the content of the slot to the player (if the slot is not empty).
                                if(!slotStack.isEmpty()) {
                                    ItemHandlerHelper.giveItemToPlayer(player, slotStack);
                                    slot.putStack(ItemStack.EMPTY);
                                }
                                //Now that the slot is empty we can transfer the items from the player inventory to the slot.
                                Utils.moveStackFromPlayerInvToSlot(player, slot, requirement.getItem(), requirement.getAmount(), requirement.getNbt());
                            } else //The player don't have the required items in its inventory, so we return an error.
                                return RecipeTransferError.NOT_ENOUGH_ITEM;

                            //Remove the slot we just inserted in from the available slots.
                            slots.remove(slot);
                        } else
                            return RecipeTransferError.INVALID_RECIPE;
                    }
                    return null;
                }
            }, entry.getKey());
        }
    }*/
}

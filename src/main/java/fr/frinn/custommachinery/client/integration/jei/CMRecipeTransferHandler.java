package fr.frinn.custommachinery.client.integration.jei;

import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.common.init.CustomMachineContainer;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.network.CTransferRecipePacket;
import fr.frinn.custommachinery.common.util.slot.SlotItemComponent;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CMRecipeTransferHandler implements IRecipeTransferHandler<CustomMachineContainer, IMachineRecipe> {

    private final RecipeType<IMachineRecipe> type;
    private final IRecipeTransferHandlerHelper transferHelper;
    private final IStackHelper stackHelper;

    public CMRecipeTransferHandler(RecipeType<IMachineRecipe> type, IRecipeTransferHandlerHelper transferHelper, IStackHelper stackHelper) {
        this.type = type;
        this.transferHelper = transferHelper;
        this.stackHelper = stackHelper;
    }

    @Override
    public Class<CustomMachineContainer> getContainerClass() {
        return CustomMachineContainer.class;
    }

    @Override
    public Optional<MenuType<CustomMachineContainer>> getMenuType() {
        return Optional.of(Registration.CUSTOM_MACHINE_CONTAINER.get());
    }

    @Override
    public RecipeType<IMachineRecipe> getRecipeType() {
        return this.type;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(CustomMachineContainer container, IMachineRecipe recipe, IRecipeSlotsView slots, Player player, boolean maxTransfer, boolean doTransfer) {
        //List of required items
        List<IRecipeSlotView> inputItemSlots = slots.getSlotViews(RecipeIngredientRole.INPUT).stream().filter(view -> view.getItemStacks().findAny().isPresent()).toList();
        if(inputItemSlots.isEmpty())
            return this.transferHelper.createInternalError();

        List<SlotItemComponent> inputSlots = container.inputSlots();
        List<Slot> inventorySlots = container.inventorySlots();

        //From - To - Amount
        List<Triple<Integer, Integer, Integer>> operations = new ArrayList<>();

        for(IRecipeSlotView view : inputItemSlots) {
            String slotId = view.getSlotName().orElse(null);
            if(slotId == null)
                return this.transferHelper.createInternalError();
            SlotItemComponent machineSlot = inputSlots.stream().filter(slotItemComponent -> slotItemComponent.getComponent().getId().equals(slotId)).findFirst().orElse(null);
            if(machineSlot == null)
                return this.transferHelper.createInternalError();

            if(!machineSlot.getItem().isEmpty() && inventorySlots.stream().noneMatch(inventorySlot -> inventorySlot.mayPlace(machineSlot.getItem())))
                return this.transferHelper.createUserErrorWithTooltip(Component.translatable("jei.tooltip.error.recipe.transfer.inventory.full"));

            Slot inventorySlotContainingIngredient = inventorySlots.stream().filter(inventorySlot -> view.getItemStacks().anyMatch(stack -> this.stackHelper.isEquivalent(stack, inventorySlot.getItem(), UidContext.Ingredient) && inventorySlot.getItem().getCount() >= stack.getCount())).findFirst().orElse(null);
            if(inventorySlotContainingIngredient == null)
                return this.transferHelper.createUserErrorForMissingSlots(Component.translatable("jei.tooltip.error.recipe.transfer.missing"), Collections.singletonList(view));

            operations.add(Triple.of(inventorySlotContainingIngredient.index, machineSlot.index, view.getItemStacks().filter(stack -> this.stackHelper.isEquivalent(stack, inventorySlotContainingIngredient.getItem(), UidContext.Ingredient)).findFirst().map(ItemStack::getCount).orElseThrow()));
        }
        if(doTransfer)
            PacketDistributor.sendToServer(new CTransferRecipePacket(container.containerId, operations, maxTransfer));
        return null;
    }
}

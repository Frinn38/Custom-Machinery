package fr.frinn.custommachinery.common.init;

import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.data.gui.SlotGuiElement;
import fr.frinn.custommachinery.common.util.SlotItemComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IIntArray;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

public class CustomMachineContainer extends Container {

    private PlayerInventory playerInv;
    public CustomMachineTile tile;
    private IIntArray recipeData;
    private boolean hasPlayerInventory = false;
    private List<SlotItemComponent> inputSlots = new ArrayList<>();

    public CustomMachineContainer(int id, PlayerInventory playerInv, CustomMachineTile tile) {
        super(Registration.CUSTOM_MACHINE_CONTAINER.get(), id);
        this.playerInv = playerInv;
        this.tile = tile;

        this.recipeData = new IIntArray() {
            @Override
            public int get(int index) {
                switch (index) {
                    case 0: return tile.craftingManager.recipeProgressTime;
                    case 1: return tile.craftingManager.recipeTotalTime;
                    default: return 0;
                }
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0:
                        tile.craftingManager.recipeProgressTime = value;
                        break;
                    case 1:
                        tile.craftingManager.recipeTotalTime = value;
                        break;
                }
            }

            @Override
            public int size() {
                return 2;
            }
        };

        this.trackIntArray(this.recipeData);

        CustomMachine machine = tile.getMachine();

        machine.getGuiElements()
            .stream()
            .filter(element -> element.getType() == Registration.PLAYER_INVENTORY_GUI_ELEMENT.get())
            .findFirst()
            .ifPresent(element -> {
                this.hasPlayerInventory = true;

                int x = element.getX() + 1;
                int y = element.getY() + 1;

                for(int i= 0; i < 3; ++i) {
                    for(int j = 0; j < 9; ++j) {
                        this.addSlot(new Slot(playerInv, j + i * 9 + 9, x + j * 18, y + i * 18));
                    }
                }

                for(int k = 0; k < 9; ++k) {
                    this.addSlot(new Slot(playerInv, k, x + k * 18, y + 58));
                }
            }
        );


        machine.getGuiElements()
            .stream()
            .filter(element -> element.getType() == Registration.SLOT_GUI_ELEMENT.get())
            .map(element -> (SlotGuiElement)element)
            .forEach(element -> {
                this.tile.componentManager.getItemHandler().flatMap(itemHandler -> itemHandler.getComponentForId(element.getId())).ifPresent(component -> {
                    SlotItemComponent slotComponent = new SlotItemComponent(component, element.getX() + 1, element.getY() + 1);
                    this.addSlot(slotComponent);
                    if (component.getMode().isInput())
                        this.inputSlots.add(slotComponent);
                });
            }
        );
    }

    public CustomMachineContainer(int id, PlayerInventory playerInv, PacketBuffer extraData) {
        this(id, playerInv, ClientHandler.getClientSideCustomMachineTile(extraData.readBlockPos()));
    }

    @ParametersAreNonnullByDefault
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
        if(!this.hasPlayerInventory)
            return ItemStack.EMPTY;

        Slot clickedSlot = this.inventorySlots.get(index);
        if(clickedSlot.getStack().isEmpty())
            return ItemStack.EMPTY;

        if(clickedSlot.inventory == this.playerInv) {
            ItemStack stack = clickedSlot.getStack().copy();
            for (SlotItemComponent slotComponent : this.inputSlots) {
                int maxInput = slotComponent.getComponent().getSpaceForItem(stack);
                if(maxInput > 0) {
                    int toInsert = Math.min(maxInput, stack.getCount());
                    slotComponent.getComponent().insert(stack.getItem(), toInsert);
                    stack.shrink(toInsert);
                }
                if(stack.isEmpty())
                    break;
            }
            if(stack.isEmpty())
                clickedSlot.decrStackSize(clickedSlot.getStack().getCount());
            else
                clickedSlot.decrStackSize(clickedSlot.getStack().getCount() - stack.getCount());
        } else {
            if (!(clickedSlot instanceof SlotItemComponent))
                return ItemStack.EMPTY;

            ItemStack stack = clickedSlot.getStack().copy();
            ItemStack remaining = ItemHandlerHelper.insertItemStacked(new PlayerInvWrapper(this.playerInv), stack, false);
            if (remaining.isEmpty())
                clickedSlot.decrStackSize(stack.getCount());
            else
                clickedSlot.decrStackSize(remaining.getCount());
        }
        return ItemStack.EMPTY;
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean canInteractWith(PlayerEntity player) {
        return true;
    }

    @ParametersAreNonnullByDefault
    @Override
    public void onContainerClosed(PlayerEntity player) {
        super.onContainerClosed(player);
        if(player instanceof ServerPlayerEntity)
            this.tile.removeTrackingPlayer((ServerPlayerEntity)player);
    }

    public double getRecipeProgressPercent() {
        return (double) this.recipeData.get(0) / (double) this.recipeData.get(1);
    }
}

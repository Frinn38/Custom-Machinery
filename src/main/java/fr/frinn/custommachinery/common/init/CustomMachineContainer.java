package fr.frinn.custommachinery.common.init;

import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.data.gui.SlotGuiElement;
import fr.frinn.custommachinery.common.network.SyncableContainer;
import fr.frinn.custommachinery.common.util.SlotItemComponent;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IWorldPosCallable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CustomMachineContainer extends SyncableContainer {

    private final PlayerInventory playerInv;
    public CustomMachineTile tile;
    private boolean hasPlayerInventory = false;
    private final Map<String, SlotItemComponent> inputSlots = new HashMap<>();

    public CustomMachineContainer(int id, PlayerInventory playerInv, CustomMachineTile tile) {
        super(Registration.CUSTOM_MACHINE_CONTAINER.get(), id, tile);
        this.playerInv = playerInv;
        this.tile = tile;

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
                this.tile.componentManager.getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get()).flatMap(itemHandler -> itemHandler.getComponentForID(element.getID())).ifPresent(component -> {
                    int x = element.getX();
                    int y = element.getY();
                    int width = element.getWidth();
                    int height = element.getHeight();
                    int slotX = x + (width - 16) / 2;
                    int slotY = y + (height - 16) / 2;
                    SlotItemComponent slotComponent = new SlotItemComponent(component, slotX, slotY);
                    this.addSlot(slotComponent);
                    if (component.getMode().isInput())
                        this.inputSlots.put(component.getId(), slotComponent);
                });
            }
        );
    }

    public CustomMachineContainer(int id, PlayerInventory playerInv, PacketBuffer extraData) {
        this(id, playerInv, ClientHandler.getClientSideCustomMachineTile(extraData.readBlockPos()));
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        this.tile.markDirty();
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
        if(!this.hasPlayerInventory)
            return ItemStack.EMPTY;

        Slot clickedSlot = this.inventorySlots.get(index);
        if(clickedSlot.getStack().isEmpty())
            return ItemStack.EMPTY;

        if(clickedSlot.inventory == this.playerInv) {
            ItemStack stack = clickedSlot.getStack().copy();
            for (SlotItemComponent slotComponent : this.inputSlots.values()) {
                int maxInput = slotComponent.getComponent().getSpaceForItem(stack);
                if(maxInput > 0) {
                    int toInsert = Math.min(maxInput, stack.getCount());
                    ItemStack stack1 = new ItemStack(stack.getItem(), toInsert);
                    stack1.setTag(stack.getTag());
                    slotComponent.getComponent().insert(stack1);
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
                clickedSlot.decrStackSize(stack.getCount() - remaining.getCount());
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        return isWithinUsableDistance(IWorldPosCallable.of(player.world, this.tile.getPos()), player, Registration.CUSTOM_MACHINE_BLOCK.get());
    }

    public double getRecipeProgressPercent() {
        if(this.tile.craftingManager.recipeTotalTime > 0)
            return this.tile.craftingManager.recipeProgressTime / (double) this.tile.craftingManager.recipeTotalTime;
        return 0;
    }

    public void elementClicked(int element, byte button) {
        if(element < 0 || element >= this.tile.getMachine().getGuiElements().size())
            throw new IllegalArgumentException("Invalid gui element ID: " + element);
        this.tile.getMachine().getGuiElements().get(element).handleClick(button, this.tile);
    }

    public Map<String, SlotItemComponent> getInputSlots() {
        return this.inputSlots;
    }
}

package fr.frinn.custommachinery.common.init;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.component.variant.item.DefaultItemComponentVariant;
import fr.frinn.custommachinery.common.component.variant.item.FilterItemComponentVariant;
import fr.frinn.custommachinery.common.component.variant.item.FuelItemComponentVariant;
import fr.frinn.custommachinery.common.component.variant.item.ResultItemComponentVariant;
import fr.frinn.custommachinery.common.component.variant.item.UpgradeItemComponentVariant;
import fr.frinn.custommachinery.common.crafting.craft.CraftProcessor;
import fr.frinn.custommachinery.common.guielement.SlotGuiElement;
import fr.frinn.custommachinery.common.network.SyncableContainer;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.common.util.slot.FilterSlotItemComponent;
import fr.frinn.custommachinery.common.util.slot.ResultSlotItemComponent;
import fr.frinn.custommachinery.common.util.slot.SlotItemComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomMachineContainer extends SyncableContainer {

    public static void open(ServerPlayer player, MachineTile machine) {
        player.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return machine.getMachine().getName();
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
                return new CustomMachineContainer(id, inv, (CustomMachineTile) machine);
            }
        }, buf -> buf.writeBlockPos(machine.getBlockPos()));
    }

    private final Inventory playerInv;
    private final CustomMachineTile tile;
    private int firstComponentSlotIndex = 0;
    private boolean hasPlayerInventory = false;
    private final List<SlotItemComponent> inputSlotComponents = new ArrayList<>();

    public CustomMachineContainer(int id, Inventory playerInv, CustomMachineTile tile) {
        super(Registration.CUSTOM_MACHINE_CONTAINER.get(), id, tile, playerInv.player);
        this.playerInv = playerInv;
        this.tile = tile;
        this.init();
        tile.startInteracting(getPlayer());
    }

    public CustomMachineContainer(int id, Inventory playerInv, FriendlyByteBuf extraData) {
        this(id, playerInv, ClientHandler.getClientSideCustomMachineTile(extraData.readBlockPos()));
    }

    @Override
    public void init() {
        super.init();
        this.slots.clear();
        this.inputSlotComponents.clear();

        AtomicInteger slotIndex = new AtomicInteger(0);

        this.tile.getGuiElements()
            .stream()
            .filter(element -> element.getType() == Registration.PLAYER_INVENTORY_GUI_ELEMENT.get())
            .findFirst()
            .ifPresent(element -> {
                this.hasPlayerInventory = true;

                int x = element.getX() + 1;
                int y = element.getY() + 1;

                for(int k = 0; k < 9; ++k)
                    this.addSyncedSlot(new Slot(this.playerInv, slotIndex.getAndIncrement(), x + k * 18, y + 58));

                for(int i= 0; i < 3; ++i) {
                    for(int j = 0; j < 9; ++j)
                        this.addSyncedSlot(new Slot(this.playerInv, slotIndex.getAndIncrement(), x + j * 18, y + i * 18));
                }
            });

        this.firstComponentSlotIndex = slotIndex.get() + 1;

        this.tile.getGuiElements()
            .stream()
            .filter(element -> element.getType() == Registration.SLOT_GUI_ELEMENT.get())
            .map(element -> (SlotGuiElement)element)
            .forEach(element ->
                this.tile.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get()).flatMap(itemHandler -> itemHandler.getComponentForID(element.getComponentId())).ifPresent(component -> {
                    int x = element.getX();
                    int y = element.getY();
                    int width = element.getWidth();
                    int height = element.getHeight();
                    int slotX = x + (width - 16) / 2;
                    int slotY = y + (height - 16) / 2;
                    SlotItemComponent slotComponent;
                    if(component.getVariant() == ResultItemComponentVariant.INSTANCE)
                        slotComponent = new ResultSlotItemComponent(component, slotIndex.getAndIncrement(), slotX, slotY);
                    else if(component.getVariant() == FilterItemComponentVariant.INSTANCE)
                        slotComponent = new FilterSlotItemComponent(component, slotIndex.getAndIncrement(), slotX, slotY);
                    else
                        slotComponent = new SlotItemComponent(component, slotIndex.getAndIncrement(), slotX, slotY);
                    this.addSlot(slotComponent);
                    if(component.getVariant() != DefaultItemComponentVariant.INSTANCE || component.getMode().isInput())
                        this.inputSlotComponents.add(slotComponent);
                })
            );
    }

    public CustomMachineTile getTile() {
        return this.tile;
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (slotId >= 0 && slotId < this.slots.size() && this.slots.get(slotId) instanceof SlotItemComponent slot && !slot.getItem().isEmpty()) {
            tile.getComponentManager().getComponent(Registration.EXPERIENCE_MACHINE_COMPONENT.get()).ifPresent(
                component -> {
                    if (component.canRetrieveFromSlots()) {
                        if (component.slotsFromCanRetrieve().isEmpty()) {
                            player.giveExperiencePoints(Utils.toInt(component.getXp()));
                            component.extractXp(component.getXp(), false);
                        } else {
                            component.slotsFromCanRetrieve().forEach(id -> {
                                if (id.equals(slot.getComponent().getId())) {
                                    player.giveExperiencePoints(Utils.toInt(component.getXp()));
                                    component.extractXp(component.getXp(), false);
                                }
                            });
                        }
                    }
                }
            );
        }
        this.tile.setChanged();
        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if(!this.hasPlayerInventory)
            return ItemStack.EMPTY;

        Slot clickedSlot = this.slots.get(index);
        if(clickedSlot.getItem().isEmpty())
            return ItemStack.EMPTY;

        if(clickedSlot.container == this.playerInv) {
            ItemStack stack = clickedSlot.getItem().copy();
            List<SlotItemComponent> components;
            if(!CustomMachinery.UPGRADES.getUpgradesForItemAndMachine(stack.getItem(), this.tile.getId()).isEmpty())
                components = this.inputSlotComponents.stream().sorted(Comparator.comparingInt(slot -> slot.getComponent().getVariant() == UpgradeItemComponentVariant.INSTANCE ? -1 : 1)).toList();
            else if(stack.getBurnTime(RecipeType.SMELTING) > 0)
                components = this.inputSlotComponents.stream().sorted(Comparator.comparingInt(slot -> slot.getComponent().getVariant() == FuelItemComponentVariant.INSTANCE ? -1 : 1)).toList();
            else
                components = this.inputSlotComponents;
            for (SlotItemComponent slotComponent : components) {
                if(slotComponent.getComponent().isLocked())
                    continue;
                int maxInput = slotComponent.getComponent().insert(stack.getItem(), stack.getCount(), null, true, true);
                if(maxInput > 0) {
                    int toInsert = Math.min(maxInput, stack.getCount());
                    slotComponent.getComponent().insert(stack.getItem(), toInsert, null, false, true);
                    stack.shrink(toInsert);
                }
                if(stack.isEmpty())
                    break;
            }
            if(stack.isEmpty())
                clickedSlot.remove(clickedSlot.getItem().getCount());
            else
                clickedSlot.remove(clickedSlot.getItem().getCount() - stack.getCount());
        } else {
            if (!(clickedSlot instanceof SlotItemComponent slotComponent) || slotComponent.getComponent().isLocked())
                return ItemStack.EMPTY;

            if(slotComponent instanceof ResultSlotItemComponent resultSlot && this.tile.getProcessor() instanceof CraftProcessor processor) {
                ItemStack removed = slotComponent.getItem().copy();
                if(!this.playerInv.add(removed))
                    return ItemStack.EMPTY;
                slotComponent.setChanged();

                int crafted = 0;
                while (processor.bulkCraft()) {
                    removed = slotComponent.getItem().copy();
                    if(!this.playerInv.add(removed))
                        return ItemStack.EMPTY;
                    slotComponent.setChanged();
                    if(crafted++ == 64 && player.getAbilities().instabuild) return ItemStack.EMPTY;
                }
                return ItemStack.EMPTY;
            }

            ItemStack removed = slotComponent.getItem();
            if(!moveItemStackTo(removed, 0, this.firstComponentSlotIndex - 1, false))
                return ItemStack.EMPTY;
            slotComponent.setChanged();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockState(this.tile.getBlockPos()) == this.tile.getBlockState() &&
                player.level().getBlockEntity(this.tile.getBlockPos()) == this.tile &&
                player.position().distanceToSqr(Vec3.atCenterOf(this.tile.getBlockPos())) <= 64;
    }

    @Override
    public boolean needFullSync() {
        return this.tile.getLevel() != null && this.tile.getLevel().getGameTime() % 100 == 0;
    }

    public void elementClicked(int element, byte button) {
        if(element < 0 || element >= this.tile.getGuiElements().size())
            throw new IllegalArgumentException("Invalid gui element ID: " + element);
        this.tile.getGuiElements().get(element).handleClick(button, this.tile, this, this.getPlayer());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if(player instanceof ServerPlayer serverPlayer)
            this.tile.stopInteracting(serverPlayer);
    }
}

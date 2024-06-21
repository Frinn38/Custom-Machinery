package fr.frinn.custommachinery.forge.transfer;

import com.google.common.collect.Maps;
import fr.frinn.custommachinery.common.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.util.transfer.ICommonItemHandler;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideMode;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ForgeItemHandler implements ICommonItemHandler {

    private final ItemComponentHandler handler;

    private final IItemHandler generalHandler;
    private final Map<Direction, SidedItemHandler> sidedHandlers = Maps.newEnumMap(Direction.class);
    private final Map<Direction, BlockCapabilityCache<IItemHandler, Direction>> neighbourStorages = Maps.newEnumMap(Direction.class);

    public ForgeItemHandler(ItemComponentHandler handler) {
        this.handler = handler;
        this.generalHandler = new SidedItemHandler(null, handler);
        for(Direction direction : Direction.values()) {
            SidedItemHandler sided = new SidedItemHandler(direction, handler);
            this.sidedHandlers.put(direction, sided);
        }
    }

    @Nullable
    public IItemHandler getCapability(@Nullable Direction side) {
        if(side == null)
            return this.generalHandler;
        else if(this.handler.getComponents().stream().anyMatch(component -> !component.getConfig().getSideMode(side).isNone()))
            return this.sidedHandlers.get(side);
        else
            return null;
    }

    @Override
    public void configChanged(RelativeSide side, SideMode oldMode, SideMode newMode) {
        if(oldMode.isNone() != newMode.isNone()) {
            Direction direction = side.getDirection(this.handler.getManager().getTile().getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
            this.handler.getManager().getTile().invalidateCapabilities();
            if(oldMode.isNone())
                this.handler.getManager().getLevel().updateNeighborsAt(this.handler.getManager().getTile().getBlockPos(), this.handler.getManager().getTile().getBlockState().getBlock());
        }
    }

    @Override
    public void invalidate() {
        this.handler.getManager().getTile().invalidateCapabilities();
    }

    @Override
    public void tick() {
        for(Direction side : Direction.values()) {
            if(this.handler.getComponents().stream().allMatch(component -> component.getConfig().getSideMode(side) == SideMode.NONE))
                continue;

            IItemHandler neighbour;

            if(this.neighbourStorages.get(side) == null || this.neighbourStorages.get(side).getCapability() == null) {
                this.neighbourStorages.put(side, BlockCapabilityCache.create(ItemHandler.BLOCK, (ServerLevel) this.handler.getManager().getLevel(), this.handler.getManager().getTile().getBlockPos().relative(side), side.getOpposite(), () -> !this.handler.getManager().getTile().isRemoved(), () -> this.neighbourStorages.remove(side)));
                if(this.neighbourStorages.get(side) != null)
                    neighbour = this.neighbourStorages.get(side).getCapability();
                else
                    continue;
            }
            else
                neighbour = this.neighbourStorages.get(side).getCapability();

            if(neighbour == null)
                continue;

            this.sidedHandlers.get(side).getSlotList().forEach(slot -> {
                if(slot.getComponent().getConfig().isAutoInput() && slot.getComponent().getConfig().getSideMode(side).isInput() && slot.getComponent().getItemStack().getCount() < slot.getComponent().getCapacity())
                    moveStacks(neighbour, slot, Integer.MAX_VALUE);

                if(slot.getComponent().getConfig().isAutoOutput() && slot.getComponent().getConfig().getSideMode(side).isOutput() && !slot.getComponent().getItemStack().isEmpty())
                    moveStacks(slot, neighbour, Integer.MAX_VALUE);
            });
        }
    }

    private void moveStacks(IItemHandler from, IItemHandler to, int maxAmount) {
        for(int i = 0; i < from.getSlots(); i++) {
            ItemStack canExtract = from.extractItem(i, maxAmount, true);
            if(canExtract.isEmpty())
                continue;

            ItemStack canInsert = ItemHandlerHelper.insertItemStacked(to, canExtract, false);
            if(canInsert.isEmpty())
                from.extractItem(i, maxAmount, false);
            else
                from.extractItem(i, canExtract.getCount() - canInsert.getCount(), false);
        }
    }
}

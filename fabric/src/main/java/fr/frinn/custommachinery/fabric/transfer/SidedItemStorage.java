package fr.frinn.custommachinery.fabric.transfer;

import fr.frinn.custommachinery.common.component.handler.ItemComponentHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class SidedItemStorage extends CombinedStorage<ItemVariant, ItemSlot> {

    private final ItemComponentHandler handler;
    @Nullable
    private final Direction side;

    public SidedItemStorage(ItemComponentHandler handler, @Nullable Direction side) {
        super(handler.getComponents().stream().map(component -> new ItemSlot(component, side)).toList());
        this.handler = handler;
        this.side = side;
    }
}

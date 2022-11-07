package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.api.network.IData;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.network.syncable.IntegerSyncable;
import fr.frinn.custommachinery.common.network.syncable.ItemStackSyncable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class SyncableContainer extends AbstractContainerMenu {

    private final ServerPlayer player;
    private final List<ISyncable<?, ?>> stuffToSync = new ArrayList<>();

    public SyncableContainer(@Nullable MenuType<?> type, int id, ISyncableStuff syncableStuff, Player player) {
        super(type, id);
        this.player = player instanceof ServerPlayer serverPlayer ? serverPlayer : null;
        syncableStuff.getStuffToSync(this.stuffToSync::add);
    }

    public abstract boolean needFullSync();

    @Override
    public void broadcastChanges() {
        if(this.player != null) {
            if(this.needFullSync()) {
                List<IData<?>> toSync = new ArrayList<>();
                for(short id = 0; id < this.stuffToSync.size(); id++)
                    toSync.add(this.stuffToSync.get(id).getData(id));
                new SUpdateContainerPacket(this.containerId, toSync).sendTo(this.player);
                return;
            }
            List<IData<?>> toSync = new ArrayList<>();
            for(short id = 0; id < this.stuffToSync.size(); id++) {
                if(this.stuffToSync.get(id).needSync())
                    toSync.add(this.stuffToSync.get(id).getData(id));
            }
            if(!toSync.isEmpty())
                new SUpdateContainerPacket(this.containerId, toSync).sendTo(this.player);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void handleData(IData<?> data) {
        short id = data.getID();
        ISyncable syncable = this.stuffToSync.get(id);
        if(syncable != null)
            syncable.set(data.getValue());
    }

    @Override
    protected DataSlot addDataSlot(DataSlot intReferenceHolder) {
        this.stuffToSync.add(IntegerSyncable.create(intReferenceHolder::get, intReferenceHolder::set));
        return intReferenceHolder;
    }

    @Override
    protected void addDataSlots(ContainerData array) {
        for(int i = 0; i < array.getCount(); i++) {
            int index = i;
            this.stuffToSync.add(IntegerSyncable.create(() -> array.get(index), integer -> array.set(index, integer)));
        }
    }

    protected Slot addSyncedSlot(Slot slot) {
        this.stuffToSync.add(ItemStackSyncable.create(slot::getItem, slot::set));
        return this.addSlot(slot);
    }
}

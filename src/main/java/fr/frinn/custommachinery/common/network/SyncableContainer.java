package fr.frinn.custommachinery.common.network;

import dev.latvian.mods.kubejs.player.InventoryListener;
import fr.frinn.custommachinery.api.network.IData;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.network.syncable.IntegerSyncable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class SyncableContainer extends AbstractContainerMenu {

    private final List<ServerPlayer> players = new ArrayList<>();
    private final List<ISyncable<?, ?>> stuffToSync = new ArrayList<>();

    public SyncableContainer(@Nullable MenuType<?> type, int id, ISyncableStuff syncableStuff) {
        super(type, id);
        syncableStuff.getStuffToSync(this.stuffToSync::add);
        this.broadcastChanges();
    }

    public abstract boolean needFullSync();

    @Override
    public void broadcastChanges() {
        if(!this.players.isEmpty()) {
            if(this.needFullSync()) {
                List<IData<?>> toSync = new ArrayList<>();
                for(short id = 0; id < this.stuffToSync.size(); id++)
                    toSync.add(this.stuffToSync.get(id).getData(id));
                this.players.forEach(player -> NetworkManager.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SUpdateContainerPacket(this.containerId, toSync)));
                return;
            }
            List<IData<?>> toSync = new ArrayList<>();
            for(short id = 0; id < this.stuffToSync.size(); id++) {
                if(this.stuffToSync.get(id).needSync())
                    toSync.add(this.stuffToSync.get(id).getData(id));
            }
            if(!toSync.isEmpty())
                this.players.forEach(player -> NetworkManager.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SUpdateContainerPacket(this.containerId, toSync)));
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

    @Override
    public void addSlotListener(ContainerListener listener) {
        super.addSlotListener(listener);
        if(listener instanceof InventoryListener inv)
            this.players.add(inv.player);
    }

    @Override
    public void removeSlotListener(ContainerListener listener) {
        super.removeSlotListener(listener);
        if(listener instanceof InventoryListener inv)
            this.players.remove(inv.player);
    }
}

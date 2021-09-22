package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.api.network.IData;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.impl.network.syncable.IntegerSyncable;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntReferenceHolder;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

public abstract class SyncableContainer extends Container {

    private List<ServerPlayerEntity> players = new ArrayList<>();
    private ISyncableStuff syncableStuff;
    private List<ISyncable<?, ?>> stuffToSync = new ArrayList<>();

    public SyncableContainer(@Nullable ContainerType<?> type, int id, ISyncableStuff syncableStuff) {
        super(type, id);
        this.syncableStuff = syncableStuff;
        syncableStuff.getStuffToSync(this.stuffToSync::add);
        this.detectAndSendChanges();
    }

    @Override
    public void detectAndSendChanges() {
        if(!this.players.isEmpty()) {
            List<IData<?>> toSync = new ArrayList<>();
            for(short id = 0; id < this.stuffToSync.size(); id++) {
                if(this.stuffToSync.get(id).needSync())
                    toSync.add(this.stuffToSync.get(id).getData(id));
            }
            if(!toSync.isEmpty())
                this.players.forEach(player -> NetworkManager.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SUpdateContainerPacket(this.windowId, toSync)));
        }
    }

    public void handleData(IData<?> data) {
        short id = data.getID();
        ISyncable syncable = this.stuffToSync.get(id);
        if(syncable != null)
            syncable.set(data.getValue());
    }

    @ParametersAreNonnullByDefault
    @Override
    protected IntReferenceHolder trackInt(IntReferenceHolder intReferenceHolder) {
        this.stuffToSync.add(IntegerSyncable.create(intReferenceHolder::get, intReferenceHolder::set));
        return intReferenceHolder;
    }

    @ParametersAreNonnullByDefault
    @Override
    protected void trackIntArray(IIntArray array) {
        for(int i = 0; i < array.size(); i++) {
            int index = i;
            this.stuffToSync.add(IntegerSyncable.create(() -> array.get(index), integer -> array.set(index, integer)));
        }
    }

    @ParametersAreNonnullByDefault
    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        if(listener instanceof ServerPlayerEntity)
            this.players.add((ServerPlayerEntity)listener);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void removeListener(IContainerListener listener) {
        super.removeListener(listener);
        this.players.remove(listener);
    }
}

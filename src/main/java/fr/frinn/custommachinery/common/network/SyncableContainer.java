package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.impl.network.Data;
import fr.frinn.custommachinery.impl.network.Syncable;
import fr.frinn.custommachinery.impl.network.SyncableList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class SyncableContainer extends AbstractContainerMenu {

    //Null on client side
    @Nullable
    protected final ServerPlayer player;
    private final List<Syncable<?>> stuffToSync = new ArrayList<>();
    private final ISyncableStuff syncableStuff;

    public SyncableContainer(@Nullable MenuType<?> type, int id, ISyncableStuff syncableStuff, Player player) {
        super(type, id);
        this.player = player instanceof ServerPlayer serverPlayer ? serverPlayer : null;
        this.syncableStuff = syncableStuff;
    }

    public void init() {
        this.stuffToSync.clear();
        SyncableList list = new SyncableList(this.stuffToSync);
        list.syncItemStack(this::getCarried, this::setCarried);
        this.syncableStuff.getStuffToSync(list);
    }

    public abstract boolean needFullSync();

    @Override
    public void broadcastChanges() {
        if(this.player != null) {
            if(this.needFullSync()) {
                List<Data<?>> toSync = new ArrayList<>();
                for(short id = 0; id < this.stuffToSync.size(); id++)
                    toSync.add(this.stuffToSync.get(id).makeData(id));
                PacketDistributor.sendToPlayer(this.player, new SUpdateContainerPacket(this.containerId, toSync));
                return;
            }
            List<Data<?>> toSync = new ArrayList<>();
            for(short id = 0; id < this.stuffToSync.size(); id++) {
                if(this.stuffToSync.get(id).needSync())
                    toSync.add(this.stuffToSync.get(id).makeData(id));
            }
            if(!toSync.isEmpty())
                PacketDistributor.sendToPlayer(this.player, new SUpdateContainerPacket(this.containerId, toSync));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void handleData(Data<?> data) {
        short id = data.id();
        Syncable syncable = this.stuffToSync.get(id);
        syncable.set(data.value());
    }

    @Override
    protected DataSlot addDataSlot(DataSlot intReferenceHolder) {
        this.stuffToSync.add(CMRegistration.INTEGER_DATA.get().createSyncable(intReferenceHolder::get, intReferenceHolder::set));
        return intReferenceHolder;
    }

    @Override
    protected void addDataSlots(ContainerData array) {
        for(int i = 0; i < array.getCount(); i++) {
            int index = i;
            this.stuffToSync.add(CMRegistration.INTEGER_DATA.get().createSyncable(() -> array.get(index), integer -> array.set(index, integer)));
        }
    }

    @Override
    public void initializeContents(int stateId, List<ItemStack> items, ItemStack carried) {

    }

    protected void addSyncedSlot(Slot slot) {
        this.stuffToSync.add(CMRegistration.ITEMSTACK_DATA.get().createSyncable(slot::getItem, slot::set));
        this.addSlot(slot);
    }
}

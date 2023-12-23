package fr.frinn.custommachinery.common.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.utils.Env;
import fr.frinn.custommachinery.common.component.variant.item.FilterItemComponentVariant;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class CSetFilterSlotItemPacket extends BaseC2SMessage {

    private final ItemStack stack;
    private final BlockPos pos;
    private final String slotID;

    public CSetFilterSlotItemPacket(ItemStack stack, BlockPos pos, String slotID) {
        this.stack = stack;
        this.pos = pos;
        this.slotID = slotID;
    }

    @Override
    public MessageType getType() {
        return PacketManager.SET_FILTER_SLOT_ITEM;
    }

    public static CSetFilterSlotItemPacket read(FriendlyByteBuf buf) {
        return new CSetFilterSlotItemPacket(buf.readItem(), buf.readBlockPos(), buf.readUtf());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeItem(this.stack);
        buf.writeBlockPos(this.pos);
        buf.writeUtf(this.slotID);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if(context.getEnvironment() == Env.SERVER)
            context.queue(() -> {
                if(context.getPlayer().level.getBlockEntity(this.pos) instanceof CustomMachineTile machine) {
                    machine.getComponentManager()
                            .getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                            .flatMap(handler -> handler.getComponentForID(this.slotID))
                            .ifPresent(component -> {
                                if(component.getVariant() == FilterItemComponentVariant.INSTANCE)
                                    component.setItemStack(this.stack);
                            });
                }
            });
    }
}
